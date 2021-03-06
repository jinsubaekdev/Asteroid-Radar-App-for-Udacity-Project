package com.udacity.asteroidradar.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import java.lang.reflect.Array.get
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    val TAG = "MainFragment"
    val SAVED_DATE = "savedDate"

    private val todayStr: String = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT).format(Date())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        val pref =
            requireContext().getSharedPreferences("com.udacity.asteroidradar", Context.MODE_PRIVATE)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = viewModel.recyclerAdapter
        binding.asteroidRecycler.layoutManager = LinearLayoutManager(requireContext())

        setHasOptionsMenu(true)

        viewModel.isAsteroidFetched.observe(viewLifecycleOwner, Observer { isFetched ->
            if(isFetched)
                viewModel.getSavedAsteroids(MainViewModel.SHOW_TYPE.SHOW_ALL)
            else
                Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
        })

        viewModel.fetchedAsteroidProperty.observe(viewLifecycleOwner, Observer { asteroids ->
            if (asteroids != null) {
                viewModel.recyclerAdapter.submitList(asteroids)
                pref.edit().putString(SAVED_DATE, todayStr).apply()
            } else {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.selectedAsteroid.observe(viewLifecycleOwner, Observer {
            it?.let {
                findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.doneNavigating()
            }
        })

        viewModel.pictureUrl.observe(viewLifecycleOwner, Observer { url ->
            Log.i(TAG, "url: $url")
            Picasso.with(requireContext())
                .load(url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(binding.activityMainImageOfTheDay)
        })

        viewModel.pictureDescription.observe(viewLifecycleOwner, Observer { description ->
            binding.activityMainImageOfTheDay.contentDescription = description
        })

        if (pref.getString(SAVED_DATE, "") != todayStr)
            viewModel.getAsteroidProperties()
        else {
            viewModel.getSavedAsteroids(MainViewModel.SHOW_TYPE.SHOW_ALL)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.next_week_asteroids -> viewModel.getAsteroidProperties()
            R.id.today_asteroids -> viewModel.getSavedAsteroids(MainViewModel.SHOW_TYPE.SHOW_TODAY, todayStr)
            R.id.saved_asteroids -> viewModel.getSavedAsteroids(MainViewModel.SHOW_TYPE.SHOW_ALL)
        }
        return true
    }
}
