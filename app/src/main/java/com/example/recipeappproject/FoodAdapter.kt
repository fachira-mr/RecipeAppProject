package com.example.recipeappproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class FoodAdapter (var ctx: Context, var resource: Int, var item: ArrayList<FoodItem>): ArrayAdapter<FoodItem>(ctx, resource, item) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(ctx)
        val view = layoutInflater.inflate(resource, null)

        val title = view.findViewById<TextView>(R.id.txt_title)
        val description = view.findViewById<TextView>(R.id.txt_description)


        title.text = item[position].title
        description.text = item[position].description

        return view
    }
}