package com.GroceerCart.sa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.R
import com.GroceerCart.sa.activity.GroceerOrderDetailListActivity
import com.GroceerCart.sa.service.orderlist.Table
import kotlinx.android.synthetic.main.activity_groceer_order_detail_list_itemm.view.*
import java.text.SimpleDateFormat
import java.util.*


class OrderListDetailAdapter(
    groceerOrderDetailListActivity: GroceerOrderDetailListActivity,
    mOrderListResponse: List<Table>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var orderListResponse:List<Table> = mOrderListResponse
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_groceer_order_detail_list_itemm, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return orderListResponse.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.textOrderNo.text = orderListResponse.get(position).orderNo
        holder.itemView.textTxDate.text = formatDate(orderListResponse.get(position).transactionDate.toString())
        holder.itemView.textStatus.text = orderListResponse.get(position).orderStatus.toString()

        /* if(orderListResponse.get(position).orderStatus!= null){
             holder.itemView.textStatus.text = orderListResponse.get(position).orderStatus.toString()
         }*/
        holder.itemView.textSubTotal.text = orderListResponse.get(position).totalAmount.toString()
        holder.itemView.textDiscount.text = orderListResponse.get(position).discountAmount.toString()
        holder.itemView.textTax.text = orderListResponse.get(position).taxAmount.toString()
        holder.itemView.textTotal.text = orderListResponse.get(position).netTotal.toString()

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textOrderNo: TextView
        var textOrderTransactionDate: TextView
        var textOrderStatus: TextView
        var textOrderSubTotal: TextView
        var textOrderDiscount: TextView
        var textOrderTax: TextView
        var textOrderTotal: TextView

        init {
            textOrderNo = itemView.findViewById(R.id.textOrderNo)
            textOrderTransactionDate = itemView.findViewById(R.id.textTxDate)
            textOrderStatus = itemView.findViewById(R.id.textStatus)
            textOrderSubTotal = itemView.findViewById(R.id.textSubTotal)
            textOrderDiscount = itemView.findViewById(R.id.textDiscount)
            textOrderTax = itemView.findViewById(R.id.textTax)
            textOrderTotal = itemView.findViewById(R.id.textTotal)

        }
    }

    private fun formatDate(dateString: String):String{
      /*  val parsedDate = LocalDateTime.parse("2018-12-14T09:55:00", DateTimeFormatter.ISO_DATE_TIME)
        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))*/
        val parser =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.US)
        val formattedDate = formatter.format(parser.parse(dateString))
        return formattedDate.toString()
    }
}