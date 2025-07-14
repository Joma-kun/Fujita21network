package com.example.internal.converter;

import com.example.internal.converter.classes.*;

import java.util.HashMap;
import java.util.Map;

public class ConverterManager {
    private static final Map<String, InstanceConverter> converters = new HashMap<>();

    static {
        converters.put("AccessList", new AccessListConverter());
        converters.put("CiscoAccessList", new AccessListConverter()); // 同じ変換器を使う別名
        converters.put("Client", new ClietntsConverter());
        converters.put("Config", new ConfigConverter());
        converters.put("EthernetSetting", new EthernetSettingConverter());
        converters.put("EthernetType", new EthernetTypeConverter());
        converters.put("Hostname", new HostnameConverter());
        converters.put("IpRoute", new IpRouteConverter());
        converters.put("Link", new LinkConverter());
        converters.put("LinkableElement", new LinkableElementConverter());
        converters.put("OspfInterfaceSetting", new OspfInterfaceSettingConverter());
        converters.put("OspfSetting", new OspfSettingConverter());
        converters.put("OspfVirtualLink", new OspfVirtualLinkConverter());
        converters.put("Stack", new StackConverter());
        converters.put("StpSetting", new StpSettingConverter());
        converters.put("Vlan", new VlanConverter());
        converters.put("VlanSetting", new VlanSettingConverter());
    }

    public static InstanceConverter getConverter(String className) {//string(クラス名)に対応したコンバーターを変えす
        return converters.get(className);
    }
}


