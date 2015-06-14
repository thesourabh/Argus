package sour.project.be.adapters;

import java.util.List;

import sour.project.be.R;
import sour.project.be.data.Controls;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ControlsAdapter extends BaseAdapter {
	private Context mContext;

	List<Controls> mControls;

	public ControlsAdapter(Context _context, List<Controls> _mControls) {
		mContext = _context;
		mControls = _mControls;
	}

	@Override
	public int getCount() {
		return mControls.size();
	}

	@Override
	public Object getItem(int position) {
		return mControls.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {

			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.controls_adapter_item,
					parent, false);

			viewHolder = new ViewHolder();
			viewHolder.tvName = (TextView) convertView
					.findViewById(R.id.tvControlName);
			viewHolder.ivIcon = (ImageView) convertView
					.findViewById(R.id.ivControlIcon);
//			 viewHolder.ivIcon.setColorFilter(mContext.getResources().getColor(R.color.red_700),
//			 Mode.SRC_ATOP);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Controls control = mControls.get(position);
		viewHolder.tvName.setText(control.name);
		viewHolder.ivIcon.setImageDrawable(control.icon);
		return convertView;
	}

	private static class ViewHolder {
		ImageView ivIcon;
		TextView tvName;
	}

}
