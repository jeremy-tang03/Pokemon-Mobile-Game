package ca.dawsoncollege.project_pokemon

import android.content.ClipData
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL

interface CustomListener {
    fun setEmptyList(visibility: Int, recyclerView: Int, emptyTextView: Int)
}

class CustomAdapter(
    private var list: List<Pokemon>,
    private val listener: CustomListener?,
    private val userDao: UserDao,
    private val context: Context
) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder?>(), View.OnTouchListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CustomViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(list: MutableList<Pokemon>) {
        this.list = list
    }

    fun getList(): MutableList<Pokemon> = this.list.toMutableList()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val data = ClipData.newPlainText("", "")
                val shadowBuilder = View.DragShadowBuilder(v)
                v?.startDragAndDrop(data, shadowBuilder, v, 0)
                return true
            }
        }
        return false
    }

    val dragInstance: DragListener?
        get() = if (listener != null) {
            DragListener(listener, userDao, context)
        } else {
            Log.e(javaClass::class.simpleName, "Listener not initialized")
            null
        }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        runBlocking {
            launch(Dispatchers.IO) {
                holder.img?.setImageBitmap(BitmapFactory.decodeStream(URL(list[position].data.frontSprite).openStream()))
            }
        }
        holder.name!!.text = list[position].name
        holder.constraintLayout?.tag = position
        holder.constraintLayout?.setOnTouchListener(this)
        holder.constraintLayout?.setOnDragListener(DragListener(listener!!, userDao, context))
    }

    class CustomViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item, parent, false)) {
        var img: ImageView? = null
        var name: TextView? = null
        var constraintLayout: ConstraintLayout? = null

        init {
            img = itemView.findViewById(R.id.front_img)
            name = itemView.findViewById(R.id.pokemonName)
            constraintLayout = itemView.findViewById(R.id.frame_layout_item)
        }
    }
}