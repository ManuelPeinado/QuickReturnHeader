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

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ListViewScrollObserver implements OnScrollListener {
    private OnListViewScrollListener listener;
    private OnScrollListener onScrollListener;
    private int dividerHeight;
    private int lastFirstVisibleItem;
    private int lastTop;
    private int scrollPosition;
    private int lastHeight;

    public interface OnListViewScrollListener {
        void onScrollUpDownChanged(int delta, int scrollPosition, int firstVisibleItem, boolean exact);
        void onScrollIdle();
    }

    public ListViewScrollObserver(ListView listView, OnScrollListener onScrollListener) {
        listView.setOnScrollListener(this);
        this.dividerHeight = listView.getDividerHeight();
        this.onScrollListener = onScrollListener;
    }

    public void setOnScrollUpAndDownListener(OnListViewScrollListener listener) {
        this.listener = listener;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View firstChild = view.getChildAt(0);
        if (firstChild == null) {
            return;
        }
        int top = firstChild.getTop();
        int height = firstChild.getHeight();
        int delta;
        int skipped = 0;
        if (lastFirstVisibleItem == firstVisibleItem) {
            delta = lastTop - top;
        } else if (firstVisibleItem > lastFirstVisibleItem) {
            skipped = firstVisibleItem - lastFirstVisibleItem - 1;
            delta = skipped * (height + dividerHeight) + lastHeight + lastTop + dividerHeight - top;
        } else {
            skipped = lastFirstVisibleItem - firstVisibleItem - 1;
            delta = skipped * -(height + dividerHeight) + lastTop - (height + dividerHeight + top);
        }
        boolean exact = skipped == 0;
        scrollPosition += -delta;
        if (listener != null) {
            listener.onScrollUpDownChanged(-delta, scrollPosition, firstVisibleItem, exact);
        }
        lastFirstVisibleItem = firstVisibleItem;
        lastTop = top;
        lastHeight = height;

        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (listener != null && scrollState == SCROLL_STATE_IDLE) {
            listener.onScrollIdle();
        }

        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }
}
