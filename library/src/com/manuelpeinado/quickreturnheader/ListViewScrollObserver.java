package com.manuelpeinado.quickreturnheader;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ListViewScrollObserver implements OnScrollListener {
    private OnListViewScrollListener listener;
    private int lastFirstVisibleItem;
    private int lastTop;
    private int scrollPosition;
    private int lastHeight;

    public interface OnListViewScrollListener {
        void onScrollUpDownChanged(int delta, int scrollPosition, boolean exact);
    }

    public ListViewScrollObserver(ListView listView) {
        listView.setOnScrollListener(this);
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
            delta = skipped * height + lastHeight + lastTop - top;
        } else {
            skipped = lastFirstVisibleItem - firstVisibleItem - 1;
            delta = skipped * -height + lastTop - (height + top);
        }
        boolean exact = skipped > 0;
        scrollPosition += -delta;
        if (listener != null) {
            listener.onScrollUpDownChanged(-delta, scrollPosition, exact);
        }
        lastFirstVisibleItem = firstVisibleItem;
        lastTop = top;
        lastHeight = firstChild.getHeight();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
