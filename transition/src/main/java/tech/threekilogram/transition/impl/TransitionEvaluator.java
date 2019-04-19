package tech.threekilogram.transition.impl;

/**
 * @author wuxio 2018-06-23:12:16
 */

import android.view.View;
import tech.threekilogram.transition.Evaluator;
import tech.threekilogram.transition.Measure;
import tech.threekilogram.transition.ViewVisionState;

/**
 * 使用该类制作静态变化效果,根据进度变化
 *
 * @author wuxio
 */
public class TransitionEvaluator implements Evaluator {

      /**
       * 作用于该view
       */
      private View            mView;
      /**
       * 起始状态
       */
      private ViewVisionState mBegin;
      /**
       * 结束状态
       */
      private ViewVisionState mEnd;

      /**
       * 当{@link #setFraction(float)}时会重新布局view,如果此值为true,那么布局时就会重新测量
       */
      private boolean isRemeasureWhenFractionChanged;

      public TransitionEvaluator ( final View view, int endLeft, int endTop, int endRight, int endBottom ) {

            final ViewVisionState end = new ViewVisionState( view, endLeft, endTop, endRight, endBottom );
            view.post( new Runnable() {

                  @Override
                  public void run ( ) {

                        setField( view, new ViewVisionState( view ), end );
                  }
            } );
      }

      public TransitionEvaluator ( final View view, final ViewVisionState end ) {

            view.post( new Runnable() {

                  @Override
                  public void run ( ) {

                        setField( view, new ViewVisionState( view ), end );
                  }
            } );
      }

      public TransitionEvaluator ( View view, ViewVisionState begin, ViewVisionState end ) {

            setField( view, begin, end );
      }

      private void setField ( View view, ViewVisionState begin, ViewVisionState end ) {

            mView = view;
            mBegin = begin;
            mEnd = end;
      }

      private void evaluate ( float fraction, ViewVisionState startValue, ViewVisionState endValue ) {

            /* 计算出当前的进度的值 */

            int left =
                (int) ( startValue.getLeft()
                    + ( endValue.getLeft() - startValue.getLeft() ) * fraction );

            int top =
                (int) ( startValue.getTop() + ( endValue.getTop() - startValue.getTop() ) * fraction );

            int right = (int) ( startValue.getRight()
                + ( endValue.getRight() - startValue.getRight() ) * fraction );

            int bottom =
                (int) ( startValue.getBottom()
                    + ( endValue.getBottom() - startValue.getBottom() ) * fraction );

            float rotation =
                startValue.getRotation()
                    + ( endValue.getRotation() - startValue.getRotation() ) * fraction;

            float alpha =
                startValue.getAlpha() + ( endValue.getAlpha() - startValue.getAlpha() ) * fraction;

            if( isRemeasureWhenFractionChanged ) {

                  Measure.remeasureViewWithExactSpec(
                      mView,
                      Math.abs( right - left ),
                      Math.abs( bottom - top )
                  );
            }

            mView.layout( left, top, right, bottom );
            mView.setRotation( rotation );
            mView.setAlpha( alpha );
      }

      @Override
      public void setFraction ( float fraction ) {

            evaluate( fraction, mBegin, mEnd );
      }

      @Override
      public View getTarget ( ) {

            return mView;
      }

      /**
       * 当{@link #setFraction(float)}时会重新布局view,如果设置为true,那么布局时就会重新测量
       *
       * @param remeasureWhenFractionChanged true:布局时重新测量
       */
      public void setRemeasureWhenFractionChanged ( boolean remeasureWhenFractionChanged ) {

            isRemeasureWhenFractionChanged = remeasureWhenFractionChanged;
      }
}