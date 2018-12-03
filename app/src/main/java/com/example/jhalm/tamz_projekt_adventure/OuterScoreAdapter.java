package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OuterScoreAdapter extends ArrayAdapter<List<Highscore.DTO>>
{
    private List<List<Highscore.DTO>> data;
    private Context context;

    public OuterScoreAdapter(Context context)
    {
        super(context, R.layout.high_score_list_layout);
        Highscore highscore = new Highscore(context);
        List<Highscore.DTO> scores = highscore.SelectAll();
        this.data = new ArrayList<List<Highscore.DTO>>();

        List<Highscore.DTO> mapScores = new ArrayList<Highscore.DTO>();

        for(int i = 0; i < scores.size(); i++)
        {
            if(i == 0)
            {
                mapScores.add(scores.get(i));
            }
            else
            {
                if(!scores.get(i).mapName.equals(mapScores.get(mapScores.size() - 1).mapName))
                {
                    this.data.add(mapScores);
                    this.add(mapScores);
                    mapScores = new ArrayList<Highscore.DTO>();
                }

                mapScores.add(scores.get(i));
            }
        }
        this.data.add(mapScores);
        this.add(mapScores);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder holder = null;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.high_score_list_layout, parent, false);
            holder = new Holder();
            holder.mapName = convertView.findViewById(R.id.map_name_list);
            holder.scores = convertView.findViewById(R.id.map_score_list_view);
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }

        holder.mapName.setText(this.data.get(position).get(0).mapName);
        holder.scores.setAdapter(new InnerScoreAdapter(this.context, this.data.get(position)));

        return convertView;
    }

    private static class Holder
    {
        TextView mapName;
        ListView scores;
    }
}
