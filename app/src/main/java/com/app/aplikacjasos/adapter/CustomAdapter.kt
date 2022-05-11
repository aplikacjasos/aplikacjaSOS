package com.app.aplikacjasos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.app.aplikacjasos.R
import com.app.aplikacjasos.model.Contact
import com.app.aplikacjasos.utils.Phone
import java.util.*

class CustomAdapter(private val dataSet: List<*>, mContext: Context) :
    ArrayAdapter<Any?>(mContext, R.layout.row_item, dataSet) {
    private class ViewHolder {
        lateinit var contactName: TextView
        lateinit var contactPhone: TextView
        lateinit var checkBox: CheckBox
    }
    override fun getCount(): Int {
        return dataSet.size
    }
    override fun getItem(position: Int): Contact {
        return dataSet[position] as Contact
    }
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val result: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView =
                LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
            viewHolder.contactName =
                convertView.findViewById(R.id.contactName)
            viewHolder.contactPhone =
                convertView.findViewById(R.id.contactPhone)
            viewHolder.checkBox =
                convertView.findViewById(R.id.checkBox)
            result = convertView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }
        val item: Contact = getItem(position)
        viewHolder.contactName.text = item.contactName
        viewHolder.contactPhone.text = Phone.parse(item.phoneNumber)
        viewHolder.checkBox.isChecked = item.isChecked

        return result
    }
}