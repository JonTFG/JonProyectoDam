import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.example.comprahorro.R
import java.util.HashMap

class CustomExpandableListAdapter(
    private val context: Context,
    private var listTitles: List<String>,
    private var listDetails: HashMap<String, MutableList<Pair<String, Double>>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return listTitles.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return listDetails[listTitles[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return listTitles[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return listDetails[listTitles[groupPosition]]?.get(childPosition) ?: Pair("", 0.0)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val title = getGroup(groupPosition) as String
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val titleTextView = view.findViewById<TextView>(android.R.id.text1)
        titleTextView.text = title
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val child = getChild(groupPosition, childPosition) as Pair<String, Double>
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(android.R.layout.simple_expandable_list_item_2, parent, false)
        val nameTextView = view.findViewById<TextView>(android.R.id.text1)
        val priceTextView = view.findViewById<TextView>(android.R.id.text2)
        nameTextView.text = child.first
        priceTextView.text = context.getString(R.string.precio_format, child.second)
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    fun updateList(newTitles: List<String>, newDetails: HashMap<String, MutableList<Pair<String, Double>>>) {
        listTitles = newTitles
        listDetails = newDetails
        notifyDataSetChanged()
    }
}

