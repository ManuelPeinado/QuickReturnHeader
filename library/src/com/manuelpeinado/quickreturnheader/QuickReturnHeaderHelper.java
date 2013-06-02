package com.manuelpeinado.quickreturnheader;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.manuelpeinado.quickreturnheader.R;
import com.manuelpeinado.quickreturnheader.ListViewScrollObserver.OnListViewScrollListener;

public class QuickReturnHeaderHelper implements OnListViewScrollListener, OnGlobalLayoutListener {
    private View realHeader;
    private int headerHeight;
    private int headerTop;
    private View dummyHeader;
    private int contentResId;
    private int headerResId;
    private boolean waitingForExactHeaderHeight = true;
    private Context context;

    public QuickReturnHeaderHelper(Context context, int contentResId, int headerResId) {
        this.context = context;
        this.contentResId = contentResId;
        this.headerResId = headerResId;
    }

    public View createView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.qrh_frame, null);

        inflater.inflate(contentResId, frame, true);

        ListView listView = (ListView) frame.findViewById(android.R.id.list);
        listView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        ListViewScrollObserver observer = new ListViewScrollObserver(listView);
        observer.setOnScrollUpAndDownListener(this);

        realHeader = inflater.inflate(headerResId, null);
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        frameParams.gravity = Gravity.TOP;
        frame.addView(realHeader, frameParams);

        // Use measured height here as an estimate of the header height, later on after the layout is complete 
        // we'll use the actual height
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.EXACTLY);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);
        realHeader.measure(widthMeasureSpec, heightMeasureSpec);
        headerHeight = realHeader.getMeasuredHeight();

        dummyHeader = new View(context);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, headerHeight);
        dummyHeader.setLayoutParams(params);
        listView.addHeaderView(dummyHeader);

        return frame;
    }

    @Override
    public void onScrollUpDownChanged(int delta, int scrollPosition, boolean exact) {
        if (delta > 0) {
            if (headerTop + delta > 0) {
                delta = -headerTop;
            }
        } else {
            if (headerTop + delta < -headerHeight) {
                delta = -(headerHeight + headerTop);
            }
        }
        headerTop += delta;
        realHeader.offsetTopAndBottom(delta);
    }

    @Override
    public void onGlobalLayout() {
        if (waitingForExactHeaderHeight && dummyHeader.getHeight() > 0) {
            headerHeight = dummyHeader.getHeight();
            waitingForExactHeaderHeight = false;
            LayoutParams params = dummyHeader.getLayoutParams();
            params.height = headerHeight;
            dummyHeader.setLayoutParams(params);
        }
    }
}
