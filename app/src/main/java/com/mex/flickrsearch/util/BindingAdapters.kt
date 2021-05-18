package com.mex.flickrsearch.util

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mex.flickrsearch.R

/**
 * Uses the Glide library to load an image by URL into the [ImageView]
 */
@BindingAdapter("pictureUrl")
fun bindImageView(imgView: ImageView, picUrl: String?) {
    picUrl?.let {

        val loadingDrawable = CircularProgressDrawable(imgView.context)
        loadingDrawable.setColorSchemeColors(
            R.attr.colorPrimary,
            R.attr.colorPrimaryDark,
            R.attr.colorAccent
        )
        loadingDrawable.centerRadius = 50f
        loadingDrawable.strokeWidth = 8f
        loadingDrawable.start()

        val imgUri = picUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .centerCrop()
            .apply(
                RequestOptions()
                    .placeholder(loadingDrawable)
                    .error(R.drawable.ic_baseline_broken_image_24)
            )
            .into(imgView)
    }
}


/*
 adapter.submit is a suspend function => needs coroutine
 currently not possible to use lifecycle aware coroutine scope in BindingAdapter
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: PagingData<Picture>?) {
    data?.let {
        val adapter = recyclerView.adapter as PictureGridAdapter
        CoroutineScope(Dispatchers.Main).launch {
            adapter.submitData(it)
        }
        /*
        Does not work
        recyclerView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            adapter.submitData(it)
        }*/
    }
}
*/