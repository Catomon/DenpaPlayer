package com.github.catomon.denpaplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class PlayerFragment : Fragment(R.layout.fragment_player) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistButton = view.findViewById<ImageButton>(R.id.playlistButton)
        playlistButton.setOnClickListener {
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, PlaylistFragment::class.java, null)
                    .addToBackStack(null)
                    .commit()
        }
    }
}