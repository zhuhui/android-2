package net.cyclestreets;

import net.cyclestreets.routing.Journey;
import net.cyclestreets.routing.Route;
import net.cyclestreets.routing.Segment;
import net.cyclestreets.routing.Waypoints;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.cyclestreets.util.TurnIcons;

public class ItineraryFragment extends ListFragment 
                               implements Route.Listener
{
  private Journey journey_ = Journey.NULL_JOURNEY;
  
	@Override
	public void onCreate(final Bundle savedInstanceState) 
	{
	  super.onCreate(savedInstanceState);
	  setListAdapter(new SegmentAdapter(getActivity(), this));
	} // onCreate

	@Override
  public void onResume() 
	{
		super.onResume();
		Route.onResume();
    Route.registerListener(this);
	} // onResume	
  
  @Override
  public void onPause()
  {
    Route.unregisterListener(this);
    super.onPause();
  } // onPause
	
	@Override
  public void onListItemClick(ListView l, View v, int position, long id)
	{
	  if(journey_.isEmpty())
	    return;
    	  
	  journey_.setActiveSegmentIndex(position);
	  ((CycleStreets)getActivity()).showMap();
	} // onListItemClick
    
  @Override
  public void onNewJourney(final Journey journey, final Waypoints waypoints)
  {
    journey_ = journey;
    setSelection(journey_.activeSegmentIndex());
  } // onNewJourney

  @Override
  public void onResetJourney()
  {
    journey_ = Journey.NULL_JOURNEY;
  } // onResetJourney

	//////////////////////////////////
	static class SegmentAdapter extends BaseAdapter
	{
	  private final ItineraryFragment itinerary_;
	  private final TurnIcons.Mapping iconMappings_;
	  private final Drawable footprints_;
	  private final LayoutInflater inflater_;
	  
	  SegmentAdapter(final Context context, final ItineraryFragment itinerary)
	  {
	    itinerary_ = itinerary;
	    iconMappings_ = TurnIcons.LoadMapping(context);
	    footprints_ = context.getResources().getDrawable(R.drawable.footprints);
	    inflater_ = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  } // SegmentAdaptor
	  
	  private Journey journey() { return itinerary_.journey_; }

	  private boolean hasSegments() 
	  {
	    return !journey().isEmpty();
    } // hasSegments
    	
		@Override
		public int getCount() 
		{ 
			return hasSegments() ? journey().segments().count() : 1; 
		} // getCount

		@Override
		public Object getItem(int position) 
		{ 
			if(!hasSegments())
				return null;
			return journey().segments().get(position); 
		} // getItem

		@Override
		public long getItemId(int position) 
		{ 
			return position; 
		} // getItemId

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) 
		{
			if(!hasSegments())
				return inflater_.inflate(R.layout.itinerary_not_available, parent, false);
			
			final Segment seg = Route.journey().segments().get(position);
			final int layout_id = position != 0 ? R.layout.itinerary_item : R.layout.itinerary_header_item;
			final View v = inflater_.inflate(layout_id, parent, false);

			final boolean highlight = (position == Route.journey().activeSegmentIndex());
		
			if(position == 0)
				setText(v, R.id.segment_bonus, seg.extraInfo(), highlight);
			setText(v, R.id.segment_distance, seg.distance(), highlight);
			setText(v, R.id.segment_cumulative_distance, seg.runningDistance(), highlight);
			setText(v, R.id.segment_time, seg.runningTime(), highlight);

			setMainText(v, R.id.segment_street, seg.turn(), seg.street(), highlight);
			setTurnIcon(v, R.id.segment_type, seg.turn(), seg.walk());
			
			if(highlight && (position != 0) && (position != getCount()-1))
				v.setBackgroundColor(Color.GREEN);
			else
				v.setBackgroundColor(getColour(seg));
			
			return v;
		} // getView
		
		private void setText(final View v, final int id, final String t, final boolean highlight)
		{
			final TextView n = (TextView)v.findViewById(id);
			if(n == null)
				return;
			n.setText(t);
			if(highlight)
				n.setTextColor(Color.BLACK);
		} // setText
		
		private void setMainText(final View v, final int id, final String turn, final String street, final boolean highlight)
		{
			String t = street;
			if(turn.length() != 0)
				t = turn + " into " + street;
			setText(v, id, t, highlight);
		} // setMainText
		
		private void setTurnIcon(final View v, final int id, final String turn, final boolean walk)		
		{
			final ImageView iv = (ImageView)v.findViewById(id);

			final Drawable icon = turnIcon(turn); 
  		iv.setImageDrawable(icon);
			if(walk)
				iv.setBackgroundDrawable(footprints_);
		} // setTurnIcon
		
		private Drawable turnIcon(final String turn)
		{
			return iconMappings_.icon(turn);
		} // turnIcon
		
		private int getColour(final Segment s)
		{
			if(s instanceof Segment.Start)
				return Color.rgb(0, 128, 0);
			if(s instanceof Segment.End)
				return Color.rgb(128, 0, 0);
			return Color.BLACK;
		} // getColour
  } // class SegmentAdaptor
} // ItineraryActivity
