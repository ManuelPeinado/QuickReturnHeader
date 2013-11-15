/*
 * Copyright (C) 2013 Manuel Peinado
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manuelpeinado.quickreturnheader;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.cyrilmottier.android.translucentactionbar.NotifyingScrollView;
import com.manuelpeinado.quickreturnheader.ListViewScrollObserver.OnListViewScrollListener;

public class QuickReturnHeaderHelper implements OnGlobalLayoutListener {
    protected static final String TAG = "QuickReturnHeaderHelper";
    private View realHeader;
    private View stickyHeader;
    private FrameLayout.LayoutParams realHeaderLayoutParams, stickyHeaderLayouParams;
    private int realHeaderHeight;
    private int headerHeight;
    private int headerTop;
    private View dummyHeader;
    private int contentResId;
    private int listResId;
    private int headerResId;
    private int stickyHeaderResId;
    private Context context;
    private ListView listView;
    private LayoutInflater inflater;
    private View content;
    private ViewGroup mContentContainer;
    private ViewGroup root;
    private int lastTop;
    private int threshold = 0;
    private boolean snapped = true;
    private OnSnappedChangeListener onSnappedChangeListener;
    private Animation animation;
    private AbsListView.OnScrollListener onScrollListener;
    /**
     * True if the last scroll movement was in the "up" direction.
     */
    private boolean scrollingUp;
    /**
     * Maximum time it takes the show/hide animation to complete. Maximum because it will take much less time if the
     * header is already partially hidden or shown.
     * <p/>
     * In milliseconds.
     */
    private static final long ANIMATION_DURATION = 400;

    public interface OnSnappedChangeListener {
        void onSnappedChange(boolean snapped);
    }

    public static class Builder {

        private Context context;
        private int contentResId, listResId, headerResId, stickyHeaderResId;
        private AbsListView.OnScrollListener onScrollListener;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setContentResId(int contentResId) {
            this.contentResId = contentResId;
            return this;
        }

        public Builder setListResId(int listResId) {
            this.listResId = listResId;
            return this;
        }

        public Builder setHeaderResId(int headerResId) {
            this.headerResId = headerResId;
            return this;
        }

        public Builder setStickyHeaderResId(int stickyHeaderResId) {
            this.stickyHeaderResId = stickyHeaderResId;
            return this;
        }

        public Builder setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
            this.onScrollListener = onScrollListener;
            return this;
        }

        public QuickReturnHeaderHelper build() {
            return new QuickReturnHeaderHelper(context, contentResId, listResId, headerResId, stickyHeaderResId, onScrollListener);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private QuickReturnHeaderHelper(Context context, int contentResId, int listResId,
                                    int headerResId, int stickyHeaderResId, AbsListView.OnScrollListener onScrollListener) {
        this.context = context;
        this.contentResId = contentResId;
        this.listResId = listResId;
        this.headerResId = headerResId;
        this.stickyHeaderResId = stickyHeaderResId;
        this.onScrollListener = onScrollListener;
    }

    public View createView() {
        inflater = LayoutInflater.from(context);
        content = inflater.inflate(contentResId, null);

        realHeader = inflater.inflate(headerResId, null);
        realHeaderLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        realHeaderLayoutParams.gravity = Gravity.TOP;

        if (stickyHeaderResId != 0) {
            stickyHeader = inflater.inflate(stickyHeaderResId, null);
            stickyHeaderLayouParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            stickyHeaderLayouParams.gravity = Gravity.TOP;
        }

        // Use measured height here as an estimate of the header height, later on after the layout is complete
        // we'll use the actual height
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);
        realHeader.measure(widthMeasureSpec, heightMeasureSpec);

        listView = (ListView) content.findViewById(listResId);
        if (listView != null) {
            createListView();
        } else {
            createScrollView();
        }
        return root;
    }

    public void setOnSnappedChangeListener(OnSnappedChangeListener onSnapListener) {
        this.onSnappedChangeListener = onSnapListener;
    }

    private void createListView() {
        root = (FrameLayout) inflater.inflate(R.layout.qrh__listview_container, null);
        root.addView(content);

        listView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        ListViewScrollObserver observer = new ListViewScrollObserver(listView, onScrollListener);

        observer.setOnScrollUpAndDownListener(new OnListViewScrollListener() {

            private boolean doScroll = false;

            @Override
            public void onScrollUpDownChanged(int delta, int scrollPosition, int firstVisibleItem, boolean exact) {

                doScroll |= firstVisibleItem == 0 || delta < 0 || delta > threshold;

                if (doScroll) {
                    onNewScroll(delta);
                    snap(headerTop == scrollPosition);
                }
            }

            @Override
            public void onScrollIdle() {
                if (doScroll) {
                    QuickReturnHeaderHelper.this.onScrollIdle();
                    doScroll = false;
                }
            }
        });

        realHeader.setId(R.id.real_header);
        root.addView(realHeader, realHeaderLayoutParams);
        if (stickyHeader != null) {
            root.addView(stickyHeader, stickyHeaderLayouParams);
        }

        setupListDummyHeader();
    }

    private void setupListDummyHeader() {
        dummyHeader = new View(context);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        dummyHeader.setLayoutParams(params);
        listView.addHeaderView(dummyHeader);
    }

    private void createScrollView() {
        root = (FrameLayout) inflater.inflate(R.layout.qrh__scrollview_container, null);

        NotifyingScrollView scrollView = (NotifyingScrollView) root.findViewById(R.id.rqh__scroll_view);
        scrollView.setOnScrollChangedListener(mOnScrollChangedListener);

        root.addView(realHeader, realHeaderLayoutParams);
        root.addView(stickyHeader, stickyHeaderLayouParams);

        mContentContainer = (ViewGroup) root.findViewById(R.id.rqh__container);
        mContentContainer.addView(content);

        dummyHeader = mContentContainer.findViewById(R.id.rqh__content_top_margin);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, headerHeight);
        dummyHeader.setLayoutParams(params);
    }

    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        @Override
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            if (t < 0) {
                onNewScroll(headerHeight - headerTop);
            } else {
                onNewScroll(lastTop - t);
            }
            if (t <= 0) {
                headerTop = 0;
            }
            snap(headerTop <= -t);
            lastTop = t;
        }

        @Override
        public void onScrollIdle() {
            QuickReturnHeaderHelper.this.onScrollIdle();
        }
    };

    /**
     * Invoked when the user stops scrolling the content. In response we might start an animation to leave the header in
     * a fully open or fully closed state.
     */
    private void onScrollIdle() {
        if (snapped) {
            // Only animate when header is out of its natural position (truly over the content).
            return;
        }
        if (headerTop > 0 || headerTop <= -headerHeight) {
            // Fully hidden, to need to animate.
            return;
        }
        if (scrollingUp) {
            hideHeader();
        } else {
            showHeader();
        }
    }

    /**
     * Shows the header using a simple downwards translation animation.
     */
    private void showHeader() {
        animateHeader(headerTop, 0);
    }

    /**
     * Hides the header using a simple upwards translation animation.
     */
    private void hideHeader() {
        animateHeader(headerTop, -headerHeight);
    }

    /**
     * Animates the marginTop property of the header between two specified values.
     *
     * @param startTop Initial value for the marginTop property.
     * @param endTop   End value for the marginTop property.
     */
    private void animateHeader(final float startTop, float endTop) {
        cancelAnimation();
        final float deltaTop = endTop - startTop;
        animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                headerTop = (int) (startTop + deltaTop * interpolatedTime);
                setViewsTopMargin();
            }
        };
        long duration = (long) (deltaTop / (float) headerHeight * ANIMATION_DURATION);
        animation.setDuration(Math.abs(duration));
        realHeader.startAnimation(animation);
    }

    private void cancelAnimation() {
        if (animation != null) {
            realHeader.clearAnimation();
            animation = null;
        }
    }

    private void onNewScroll(int delta) {

        cancelAnimation();
        if (delta > 0) {
            if (headerTop + delta > 0) {
                delta = -headerTop;
            }
        } else if (delta < 0) {
            if (headerTop + delta < -headerHeight) {
                delta = -(headerHeight + headerTop);
            }
        } else {
            return;
        }
        scrollingUp = delta < 0;
        headerTop += delta;
        // I'm aware that offsetTopAndBottom is more efficient, but it gave me trouble when scrolling to the bottom of the list
        if (realHeaderLayoutParams.topMargin != headerTop) {
            setViewsTopMargin();
        }
    }

    private void setViewsTopMargin() {
        realHeaderLayoutParams.topMargin = headerTop;
        realHeader.setLayoutParams(realHeaderLayoutParams);
        if (stickyHeader != null) {
            stickyHeaderLayouParams.topMargin = Math.max(realHeaderHeight + headerTop, 0);
            stickyHeader.setLayoutParams(stickyHeaderLayouParams);
        }
    }

    private void snap(boolean newValue) {
        if (snapped == newValue) {
            return;
        }
        snapped = newValue;
        if (onSnappedChangeListener != null) {
            onSnappedChangeListener.onSnappedChange(snapped);
        }
    }

    public View getStickyHeader() {
        return stickyHeader;
    }

    public View getQuickReturnHeader() {
        return realHeader;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void onGlobalLayout() {
        int auxRealHeaderHeight = realHeader.getVisibility() == View.VISIBLE ? realHeader.getHeight() : 0;
        int auxStickyHeaderHeight = stickyHeader != null && stickyHeader.getVisibility() == View.VISIBLE ? stickyHeader.getHeight() : 0;
        if (auxRealHeaderHeight + auxStickyHeaderHeight != headerHeight) {
            realHeaderHeight = realHeader.getHeight();
            headerHeight = auxRealHeaderHeight + auxStickyHeaderHeight;
            LayoutParams params = dummyHeader.getLayoutParams();
            params.height = headerHeight;
            dummyHeader.setLayoutParams(params);

            setViewsTopMargin();
        }
    }
}
