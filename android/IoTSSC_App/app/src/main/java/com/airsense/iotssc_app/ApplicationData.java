package com.airsense.iotssc_app;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import com.airsense.iotssc_app.adapter.BluetoothReceiver;
import com.airsense.iotssc_app.adapter.DiscoveredBluetoothDevice;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;

import java.util.List;


public class ApplicationData extends Application {
    private List<DiscoveredBluetoothDevice> discoveredBluetoothDevices;
    private BluetoothManager bluetoothManager;
    private SimpleBluetoothDeviceInterface deviceInterface;
    private BluetoothReceiver bluetoothReceiver;
    private BluetoothAdapter bluetoothAdapter;

    public List<DiscoveredBluetoothDevice> getDiscoveredBluetoothDevices() { return discoveredBluetoothDevices; }

    public void setDiscoveredBluetoothDevices(List<DiscoveredBluetoothDevice> discoveredBluetoothDevices) {
        this.discoveredBluetoothDevices = discoveredBluetoothDevices;
    }

    public BluetoothManager getBluetoothManager() { return bluetoothManager; }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public SimpleBluetoothDeviceInterface getDeviceInterface() { return deviceInterface; }

    public void setDeviceInterface(SimpleBluetoothDeviceInterface deviceInterface) {
        this.deviceInterface = deviceInterface;
    }

    public BluetoothReceiver getBluetoothReceiver() { return bluetoothReceiver; }

    public void setBluetoothReceiver(BluetoothReceiver bluetoothReceiver) { this.bluetoothReceiver = bluetoothReceiver; }

    public BluetoothAdapter getBluetoothAdapter() { return bluetoothAdapter; }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public List<DiscoveredBluetoothDevice> getDevices() {
        return discoveredBluetoothDevices;
    }

    public void setDevices(List<DiscoveredBluetoothDevice> devices) {
        this.discoveredBluetoothDevices = devices;
    }
}
