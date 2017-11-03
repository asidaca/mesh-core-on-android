package com.blxble.meshpanel.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anarchy.classify.simple.PrimitiveSimpleAdapter;
import com.blxble.meshpanel.db.DeviceNode;
import com.blxble.meshpanel.db.DeviceNodeGroup;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public abstract class FolderAdapter<VH extends FolderAdapter.ViewHolder> extends PrimitiveSimpleAdapter<List<DeviceNode>, VH> {
    protected List<DeviceNodeGroup> mData;

    public FolderAdapter(List<DeviceNodeGroup> data) {
        mData = data;
    }

    @SuppressWarnings("unchecked")
    protected VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.anarchy.classify.R.layout.simple_item, parent, false);
        return (VH) new ViewHolder(view);
    }


    protected void onBindMainViewHolder(VH holder, int position) {
    }

    protected void onBindSubViewHolder(VH holder, int mainPosition, int subPosition) {
    }

    /**
     * @param parentIndex
     * @param index       if -1  in main region
     */
    protected void onItemClick(View view, int parentIndex, int index) {
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * 副层级的数量，用于主层级上的显示效果
     *
     * @param parentPosition
     * @return
     */
    @Override
    protected int getSubItemCount(int parentPosition) {
        return mData.get(parentPosition).getDeviceNodeList().size();
    }

    @Override
    protected boolean canMergeItem(int selectPosition, int targetPosition) {
        if (selectPosition < 0) {
            return false;
        }
        DeviceNodeGroup currentSelected = mData.get(selectPosition);
        List<DeviceNode> deviceNodeList = currentSelected.getDeviceNodeList();
        return deviceNodeList.size() < 2;
    }

    /**
     * 合并数据处理
     *
     * @param selectedPosition
     * @param targetPosition
     */
    @Override
    protected void onMerged(int selectedPosition, int targetPosition) {
        List<DeviceNode> deviceNodeList = mData.get(targetPosition).getDeviceNodeList();
        DeviceNode deviceNode = mData.get(selectedPosition).getDeviceNodeList().get(0);
        if (deviceNodeList.size() == MockFolderAdapter.ITEM_SINGLE_DEVICE) {
            mData.get(targetPosition).setGroupName(deviceNode.getName());
        }
        deviceNodeList.add(deviceNode);
        mData.get(targetPosition).setDeviceNodeList(deviceNodeList);
        mData.get(targetPosition).isGroup = true;
        mData.get(targetPosition).save();
        DataSupport.delete(DeviceNodeGroup.class, mData.get(selectedPosition).getId());
        mData.remove(selectedPosition);
    }


    /**
     * 能否弹出次级窗口
     *
     * @param position    主层级点击的位置
     * @param pressedView 点击的view
     * @return
     */
    @Override
    protected boolean canExplodeItem(int position, View pressedView) {
        if (position < mData.size() && mData.get(position).getDeviceNodeList().size() > 1) {
            return true;
        }
        return false;
    }

    /**
     * 返回副层级的数据源
     *
     * @param parentPosition
     * @return
     */
    @NonNull
    @Override
    protected List<DeviceNode> getSubSource(int parentPosition) {
        return mData.get(parentPosition).getDeviceNodeList();
    }

    @Override
    protected void onMove(int selectedPosition, int targetPosition) {
        DeviceNodeGroup list = mData.remove(selectedPosition);
        mData.add(targetPosition, list);
    }

    /**
     * 副层级数据移动处理
     *
     * @param deviceNodeList         副层级数据源
     * @param selectedPosition 当前选择的item位置
     * @param targetPosition   要移动到的位置
     */
    @Override
    protected void onSubMove(List<DeviceNode> deviceNodeList, int selectedPosition, int targetPosition) {
        deviceNodeList.add(targetPosition, deviceNodeList.remove(selectedPosition));
    }

    /**
     * 从副层级移除的元素
     *
     * @param deviceNodeList        副层级数据源
     * @param selectedPosition 将要冲副层级移除的数据
     * @return 返回的数为添加到主层级的位置
     */
    @Override
    protected int onLeaveSubRegion(int parentPosition, List<DeviceNode> deviceNodeList, int selectedPosition) {
        DeviceNode deviceNode = deviceNodeList.remove(selectedPosition);
        if(deviceNodeList.size() == 0){
            mData.remove(parentPosition);
        }
        DeviceNodeGroup deviceNodeGroup = new DeviceNodeGroup();
        List<DeviceNode> deviceNodeListNew = new ArrayList<>();
        deviceNodeListNew.add(deviceNode);
        deviceNodeGroup.setDeviceNodeList(deviceNodeListNew);
        mData.add(0, deviceNodeGroup);
        mData.get(0).save();
        return mData.size() - 1;
    }


    public static class ViewHolder extends PrimitiveSimpleAdapter.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void notifyItemRemoved(){
        while (!mData.isEmpty()) {
            getMainAdapter().notifyItemRemoved(mData.size()-1);
            mData.remove(mData.size()-1);
        }
        mData.clear();
    }
}
