package com.example.rihohi.baidumap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.UIMsg;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;

public class MainActivity extends AppCompatActivity {

    public LocationClient mlocationclient;
    private TextView positionText;
    //声明LocationClient类实例并配置定位参数
    LocationClientOption locationOption = new LocationClientOption();
    private MapView mMapView=null;
    //操控地图类
    private BaiduMap mBaiduMap=null;
    //定位类
    MyLocationData.Builder mlocationBuilder=new MyLocationData.Builder();
    private boolean isfirst=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mmapview);
        mBaiduMap= mMapView.getMap();
        mlocationclient=new LocationClient(getApplicationContext());
        mlocationclient.registerLocationListener(new MylocationClient());
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        List<String>permissionList=new ArrayList<>();
        //判断权限
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
        .ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
        .READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[]permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else  {
            initLocationOption();//定位
            mlocationclient.start();
        }
    }

    private void navigateTo(BDLocation location){
        if(isfirst){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(15f);
            mBaiduMap.animateMapStatus(update);
            //显示自己
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
//            isfirst=false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"本应用需要权限",Toast.LENGTH_SHORT).show();
                        }
                    }
                    mlocationclient.start();
                }else{
                    Toast.makeText(this,"不知名错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                default:
        }
    }
    private void initLocationOption() {
//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
//可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//开始定位
        mlocationclient.setLocOption(locationOption);
    }
    /**
     * 实现定位回调
     */
    public class MylocationClient implements BDLocationListener{
        @Override
        public void onReceiveLocation(final BDLocation location) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    StringBuilder mstring=new StringBuilder();
//                    mstring.append("纬度： ").append(location.getLatitude()).append("\n");
//                    mstring.append("经度： ").append(location.getLongitude()).append("\n");
//                    mstring.append("定位方式: ");
//                    if(location.getLocType()==BDLocation.TypeGpsLocation){
//                        mstring.append("GPS").append("\n");
//                    }else if(location.getLocType()==BDLocation.TypeNetWorkLocation)
//                        mstring.append("网络").append("\n");
//                    String addr = location.getAddrStr();    //获取详细地址信息
//                    String country = location.getCountry();    //获取国家
//                    String province = location.getProvince();    //获取省份
//                    String city = location.getCity();    //获取城市
//                    String district = location.getDistrict();    //获取区县
//                    String street = location.getStreet();    //获取街道信息
//                    initLocationOption();
//                    positionText.setText(addr);
//                }
//            });
            //定位到用户
            Log.w("first",location.getAddrStr());
            if(location.getLocType()==BDLocation.TypeGpsLocation){
                Log.w("first","GPS");
                    }else if(location.getLocType()==BDLocation.TypeNetWorkLocation)
                Log.w("first","NET");
            if(location.getLocType()==BDLocation.TypeGpsLocation
                    ||location.getLocType()==BDLocation.TypeNetWorkLocation)
                navigateTo(location);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
    }
}
