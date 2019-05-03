package com.apps.spacewarf.projetfinal.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.apps.spacewarf.projetfinal.R
import com.apps.spacewarf.projetfinal.database.PlaceModel

class PlacesAdapter(context: Context, private val dataSource: List<PlaceModel>) : BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val place = getItem(position) as PlaceModel
        val rowView = inflater.inflate(R.layout.list_item_place, parent, false)

        val titleTextView = rowView.findViewById(R.id.place_name) as TextView
        titleTextView.text = place.name

        val coordinatesTextView = rowView.findViewById(R.id.place_coordinates) as TextView
        coordinatesTextView.text = "Lat:  ${place.latitude}\nLng:  ${place.longitude}"

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }
}