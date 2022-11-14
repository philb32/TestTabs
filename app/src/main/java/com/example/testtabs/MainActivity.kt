package com.example.testtabs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testtabs.ui.theme.TestTabsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestTabsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MyScreen(hostViewModel)
                }
            }
        }
    }
}

data class HostRecord(val serviceName: String, val hostName : String, val ip4addr : String)

class HostViewModel: ViewModel() {
    val itemLiveData: LiveData<SnapshotStateList<HostRecord>>
        get() = items

    private val items = MutableLiveData<SnapshotStateList<HostRecord>>()
    private val itemImpl = mutableStateListOf<HostRecord>()

    fun updateHostList(host: HostRecord){
        if (itemImpl.contains(host)) {
            Log.i("updateHostList", "Already have entry for ${host.hostName}")
            return
        }
        // Here can also check host.hostName etc to make sure it's something we're interested in,
        // For example :
        // if (!host.hostName.startsWith("myspeaker")) {
        //     Log.i("updateHostList", "Not my speaker : ${host}")
        //     return
        // }
        itemImpl.add(host)
        items.postValue(itemImpl)
    }
}

var hostViewModel = HostViewModel()

@Composable
fun MyScreen (
    model: HostViewModel
) {
    AppTabs(model)
}

@Composable
fun AppTabs (
    model: HostViewModel
) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("Onboarded Speakers", "Not Onboarded Speakers", "TAB 3")

    Column {
        TabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = state == index,
                    onClick = { state = index }
                )
            }
        }
        when (state) {
            0 -> Tab1(model)
            1 -> Tab2(model)
            2 -> Tab3(model)
        }
    }
}

@Composable
fun Tab1(
    model: HostViewModel
) {
    Column {
        val items by model.itemLiveData.observeAsState()
        Button(
            modifier = Modifier.padding(all = 8.dp),
            onClick = {
                items?.clear()
                App.startNsdWorker() },
        ) { Text("Scan") }
        items?.let { ListOfHosts(it) }
    }
}

@Composable
fun Tab2(
    model: HostViewModel
) {
    Column {
        TextButton(onClick = {}) { Text("Tab2/Button1") }
        TextButton(onClick = {}) { Text("Tab2/Button2") }
        Text(text = "TODO: Scan for speakers running softAP",
            modifier = Modifier.padding(all = 8.dp))
    }
}

@Composable
fun Tab3(
    model: HostViewModel
) {
    Column {
        TextButton(onClick = {}) { Text("Tab3/Button1") }
        TextButton(onClick = {}) { Text("Tab3/Button2") }
        Text(text = "TODO: Not sure what to do with this tab yet",
            modifier = Modifier.padding(all = 8.dp))
    }
}

@Composable
fun HostCard(host: HostRecord) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Column () {
            Text(
                text = host.serviceName,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = host.hostName,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = host.ip4addr,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
fun ListOfHosts(hostList: SnapshotStateList<HostRecord>) {
    var state by remember { mutableStateOf(0) }
    LazyColumn{
        hostList.map { item { HostCard(it) } }
    }
}

