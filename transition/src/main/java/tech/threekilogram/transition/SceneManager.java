package tech.threekilogram.transition;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import java.util.ArrayList;
import tech.threekilogram.transition.evaluator.Evaluator;
import tech.threekilogram.transition.evaluator.view.ViewEvaluator;
import tech.threekilogram.transition.evaluator.view.VisionStateEvaluator;

/**
 * 根据view在不同布局中的显示状态（位置，角度，mAlpha）创建场景动画
 * <p>
 * create scene transition
 *
 * @author wuxio 2018-06-24:16:02
 */
public class SceneManager {

      /**
       * this list contains all {@link Evaluator} of child in both scene,use {@link Evaluator} to
       * changeView VisionState
       * <p>
       * use list because the order must not changed when animate
       */
      private ArrayList<Evaluator> mEvaluators = new ArrayList<>();

      /**
       * @param targetGroup this is beginScene, it's child could change visionState to visionState
       *     defined by {@code layoutEndSceneID}
       * @param layoutEndSceneID end scene will inflate from this layout, end scene decide child in
       *     begin scene's end vision state
       *     <p>
       *     note : {@code targetGroup} must layout finished
       *     <p>
       *     note : the two scene must has same children, only children's visionState different and
       *     scene size different
       */
      public SceneManager ( final ViewGroup targetGroup, @LayoutRes final int layoutEndSceneID ) {

            LayoutInflater inflater = LayoutInflater.from( targetGroup.getContext() );
            ViewGroup sceneEnd = (ViewGroup) inflater.inflate( layoutEndSceneID, null );
            init( targetGroup, sceneEnd );
      }

      /**
       * @param targetGroup this is beginScene, it's child could change visionState to visionState
       *     defined by {@code layoutEndSceneID}
       * @param sceneEnd this is end scene , end scene decide child in begin scene's end vision
       *     state
       *     <p>
       *     note : {@code targetGroup} must layout finished
       *     <p>
       *     note : the two scene must has same children, only children's visionState different and
       *     scene size different
       */
      public SceneManager ( final ViewGroup targetGroup, final ViewGroup sceneEnd ) {

            init( targetGroup, sceneEnd );
      }

      private void init ( final ViewGroup targetGroup, final ViewGroup sceneEnd ) {

            targetGroup.post( new Runnable() {

                  @Override
                  public void run ( ) {

                        measureLayoutScene( sceneEnd, targetGroup.getWidth(), targetGroup.getHeight() );
                        createChildrenEvaluator( targetGroup, sceneEnd );
                  }
            } );
      }

      /**
       * if end scene is inflate from layout, must measure and layout it, then could compare with
       * begin scene
       *
       * @param scene need measure and layout
       * @param width use this to measure scene
       * @param height use this to measure scene
       */
      private void measureLayoutScene ( ViewGroup scene, int width, int height ) {

            int widthSpec = MeasureSpec.makeMeasureSpec( width, MeasureSpec.EXACTLY );
            int heightSpec = MeasureSpec.makeMeasureSpec( height, MeasureSpec.EXACTLY );

            scene.measure( widthSpec, heightSpec );
            scene.layout( 0, 0, scene.getMeasuredWidth(), scene.getMeasuredHeight() );
      }

      /**
       * compare children in two scene , then make Evaluator end run when animator running
       *
       * @param start start this scene end {@code end} scene
       * @param end decide view at {@code start} end visionState
       */
      @SuppressWarnings("UnnecessaryLocalVariable")
      private void createChildrenEvaluator (
          ViewGroup start,
          ViewGroup end ) {

            int count = start.getChildCount();
            for( int i = 0; i < count; i++ ) {

                  View childAtStart = start.getChildAt( i );
                  int childId = childAtStart.getId();
                  View childAtEnd = end.findViewById( childId );

                  if( childAtEnd != null ) {

                        ViewVisionState childStartState = new ViewVisionState( childAtStart );
                        ViewVisionState childEndState = new ViewVisionState( childAtEnd );
                        VisionStateEvaluator evaluator = new VisionStateEvaluator( childAtStart, childStartState, childEndState );
                        mEvaluators.add( evaluator );

                        /* if childAtStart is viewGroup compare it's children with child find start scene provideVisionState */
                        if( childAtEnd instanceof ViewGroup && childAtStart instanceof ViewGroup ) {
                              createChildrenEvaluator( (ViewGroup) childAtStart, (ViewGroup) childAtEnd );
                        }

                        //remeasure0SizeViewInBeginScene( childAtStart, childAtEnd );
                        /* remove the compared view , short time */
                        end.removeView( childAtEnd );
                  }
            }
      }

      /**
       * if view in begin Scene is 0 size(width && height is 0),measure it with size defined at end
       * scene
       */
      @Deprecated
      private void remeasure0SizeViewInBeginScene ( View beginChild, View childById ) {

            if( beginChild.getMeasuredWidth() == 0 && beginChild.getMeasuredHeight() == 0 ) {

                  Measure.remeasureViewWithExactSpec(
                      beginChild,
                      childById.getRight() - childById.getLeft(),
                      childById.getBottom() - childById.getTop()
                  );
            }
      }

      /**
       * call this will get the child evaluator
       *
       * @param childId child Id
       *
       * @return child evaluator
       */
      public Evaluator getChildEvaluator ( @IdRes int childId ) {

            ArrayList<Evaluator> evaluators = mEvaluators;

            if( evaluators != null ) {

                  int size = evaluators.size();
                  for( int i = 0; i < size; i++ ) {

                        Evaluator evaluator = evaluators.get( i );

                        if( evaluator.getTarget().getId() == childId ) {
                              return evaluator;
                        }
                  }
            }

            return null;
      }

      /**
       * call this will update the child evaluator
       *
       * @param childId child Id
       *
       * @return child evaluator
       */
      public void updateChildEvaluator ( @IdRes int childId, ViewEvaluator evaluator ) {

            ArrayList<Evaluator> evaluators = mEvaluators;
            if( evaluators != null ) {
                  int size = evaluators.size();
                  for( int i = 0; i < size; i++ ) {
                        Evaluator temp = evaluators.get( i );
                        if( temp.getTarget().getId() == childId ) {
                              evaluators.set( i, evaluator );
                        }
                  }
            }
      }

      /**
       * notify all children the animate process changed, evaluator need update
       *
       * @param process new process
       */
      public void evaluate ( float process ) {

            ArrayList<Evaluator> temp = mEvaluators;
            if( temp != null ) {

                  /* update all evaluator's process */
                  int size = temp.size();
                  for( int i = 0; i < size; i++ ) {

                        Evaluator evaluator = temp.get( i );
                        evaluator.evaluate( process );
                  }
            }
      }
}
