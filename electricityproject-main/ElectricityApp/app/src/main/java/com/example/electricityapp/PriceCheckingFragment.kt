package com.example.electricityapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.electricityapp.databinding.FragmentPriceCheckingBinding
import com.example.electricityapp.datatypes.TimeValue.TimeValue
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aatools.AAGradientColor
import com.github.aachartmodel.aainfographics.aatools.AALinearGradientDirection
import com.google.gson.GsonBuilder
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PriceCheckingFragment : Fragment() {
    private var _binding: FragmentPriceCheckingBinding? = null
    private val binding get() = _binding!!
    val gson = GsonBuilder().setPrettyPrinting().create()

    // Initialize with these values
    private var day = 1
    private var month = 1
    private var year = 2014
    private var precision = 3
    private val baseUrl = "http://boomstreet.asuscomm.com/api/rates/"
    private var urlAttachment = "daily_by_hour/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPriceCheckingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up dropdown spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.picker_precision_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.precisionSpinner.adapter = adapter
            }
        }

        binding.precisionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 0 = daily, 1 = monthly, 2 = yearly
                if (position == 0) {
                    updatePickerPrecision(3)
                }
                if (position == 1) {
                    updatePickerPrecision(2)
                }
                if (position == 2) {
                    updatePickerPrecision(1)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Set up year picker
        val currentYear = Calendar.getInstance()[Calendar.YEAR] // Get the current year
        binding.yearPicker.minValue = 2014
        binding.yearPicker.maxValue = currentYear
        binding.yearPicker.wrapSelectorWheel = false // Disable wrapping of values
        binding.yearPicker.descendantFocusability =
            NumberPicker.FOCUS_BLOCK_DESCENDANTS // Disable focusability
        binding.yearPicker.displayedValues =
            getInBetween(2014, currentYear) // Set the display values to an array of years

        // Set up month picker
        binding.monthPicker.minValue = 1
        binding.monthPicker.maxValue = 12
        binding.monthPicker.wrapSelectorWheel = false // Disable wrapping of values
        binding.monthPicker.descendantFocusability =
            NumberPicker.FOCUS_BLOCK_DESCENDANTS // Disable focusability
        binding.monthPicker.displayedValues =
            getInBetween(1, 12) // Set the display values to an array of months

        // Initialize day picker for the default values
        updateDayPicker(month, year)

        // Update on scroll
        var handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        binding.yearPicker.setOnValueChangedListener { _, _, newVal ->
            year = newVal
            updateDayPicker(month, year)
            // Cancel any existing runnable
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable {
                getPrices(baseUrl + urlAttachment + getUnixTimestamp(day, month, year))
            }
            // Post the runnable to run after 500 milliseconds
            handler.postDelayed(runnable!!, 500)
        }

        binding.monthPicker.setOnValueChangedListener { _, _, newVal ->
            month = newVal
            updateDayPicker(month, year)
            // Cancel any existing runnable
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable {
                getPrices(baseUrl + urlAttachment + getUnixTimestamp(day, month, year))
            }
            // Post the runnable to run after 500 milliseconds
            handler.postDelayed(runnable!!, 500)
        }

        binding.dayPicker.setOnValueChangedListener { _, _, newVal ->
            day = newVal
            // Cancel any existing runnable
            runnable?.let { handler.removeCallbacks(it) }
            runnable = Runnable {
                getPrices(baseUrl + urlAttachment + getUnixTimestamp(day, month, year))
            }
            // Post the runnable to run after 500 milliseconds
            handler.postDelayed(runnable!!, 500)
        }

        // Set pickers to today
        year = Calendar.getInstance()[Calendar.YEAR]
        month = Calendar.getInstance()[Calendar.MONTH] + 1
        day = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        updateDayPicker(month, year)
        binding.yearPicker.value = year
        binding.monthPicker.value = month
        binding.dayPicker.value = day

        return root
    }

    private fun getPrices(url : String) {
        binding.spinKit.visibility = View.VISIBLE
        // Request a string response from the provided URL.
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                var rows: List<TimeValue> = gson.fromJson(response, Array<TimeValue>::class.java).toList()
                val times = mutableListOf<String>()
                val prices = ArrayList<Float>()

                for (item: TimeValue in rows) {

                    // Make the x axis show the current hour, day or month
                    // instead of always starting from 0
                    var parsedValue = ""
                    if (precision == 3) {
                        parsedValue = item.time.substring(11, 16)
                    }
                    if (precision == 2) {
                        // Don't question the toInt().toString(), it's to remove
                        // the leading zeros from the day number.
                        parsedValue = item.time.substring(8, 10).toInt().toString()
                    }
                    if (precision == 1) {
                        val index = item.time.substring(5, 7).toInt()
                        // Get the months translations, and pass those
                        val monthsArray = resources.getStringArray(R.array.months_array)
                        parsedValue = monthsArray[index - 1].toString()
                    }

                    // Add to the respective Lists
                    times.add(parsedValue)
                    prices.add(item.value.toFloat())
                }

                val aaChartModel : AAChartModel = AAChartModel()
                    .chartType(AAChartType.Spline)
                    .backgroundColor("#ffffff00")
                    .dataLabelsEnabled(true)
                    .legendEnabled(false)
                    .yAxisTitle("")
                    .markerRadius(0)
                    .gradientColorEnable(true)
                    .tooltipValueSuffix( " " + getString(R.string.cents_per_kilowatt_hour))
                    .colorsTheme(arrayOf(
                        AAGradientColor.linearGradient(AALinearGradientDirection.ToTop, "#97FF56", "#FF6B49"),))
                    .categories(times.toTypedArray())
                    .series(arrayOf(
                        AASeriesElement()
                            .name(getString(R.string.price))
                            .data(prices.toArray())
                    )
                    )

                binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)
                binding.spinKit.visibility = View.INVISIBLE

            },
            Response.ErrorListener {
                // typically this is a connection error
                Log.d("ERROR", it.toString())
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {

                // basic headers for the data
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }

        // Add the request to the RequestQueue. This has to be done in both getting and sending new data.
        // if using this in an activity, use "this" instead of "context"
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)

    }

    private fun getInBetween(startNum: Int, endNum: Int): Array<String?> {
        val numInBetween = endNum - startNum + 1
        val numbers = arrayOfNulls<String>(numInBetween)
        for (i in 0 until numInBetween) {
            numbers[i] = (startNum + i).toString()
        }
        return numbers
    }

    private fun getUnixTimestamp(day: Int, month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, 0, 0, 0) // set to the beginning of the day
        calendar.set(Calendar.MILLISECOND, 0) // set milliseconds to 0
        return calendar.timeInMillis / 1000 // divide by 1000 to convert to Unix timestamp
    }

    private fun updateDayPicker(month: Int, year: Int) {
        // Set up day picker for a specific month and year
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year) // Set the year
        calendar.set(Calendar.MONTH, month - 1) // Set the month
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // Get the max day of the specified month
        val lastValue = binding.dayPicker.value
        binding.dayPicker.value = 1
        binding.dayPicker.minValue = 1
        binding.dayPicker.maxValue = maxDay
        binding.dayPicker.wrapSelectorWheel = false // Disable wrapping of values
        binding.dayPicker.descendantFocusability =
            NumberPicker.FOCUS_BLOCK_DESCENDANTS // Disable focusability
        binding.dayPicker.displayedValues =
            getInBetween(1, maxDay) // Set the display values to an array of days

        if (lastValue > maxDay) {
            binding.dayPicker.value = maxDay
        }
        else {
            binding.dayPicker.value = lastValue
        }
    }

    private fun updatePickerPrecision(newPrecision: Int) {
        precision = newPrecision
        if (precision == 3) {
            binding.dayPicker.visibility = View.VISIBLE
            binding.monthPicker.visibility = View.VISIBLE
            binding.yearPicker.visibility = View.VISIBLE
            urlAttachment = "daily_by_hour/"
        }
        if (precision == 2) {
            binding.dayPicker.visibility = View.GONE
            binding.monthPicker.visibility = View.VISIBLE
            binding.yearPicker.visibility = View.VISIBLE
            urlAttachment = "monthly_by_day/"
        }
        if (precision == 1) {
            binding.dayPicker.visibility = View.GONE
            binding.monthPicker.visibility = View.GONE
            binding.yearPicker.visibility = View.VISIBLE
            urlAttachment = "yearly_by_month/"
        }
        getPrices(baseUrl + urlAttachment + getUnixTimestamp(day,month, year))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
