package com.example.data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
public class OspfData {
    @JsonProperty("ospfAreas")
    private List<OspfArea> ospfAreas;

    public List<OspfArea> getOspfAreas() {
        return ospfAreas;
    }

    public void setOspfAreas(List<OspfArea> ospfAreas) {
        this.ospfAreas = ospfAreas;
    }

    public static class OspfArea {
        @JsonProperty("areaId")
        private int areaId;

        @JsonProperty("devices")
        private List<Device> devices;

        public int getAreaId() {
            return areaId;
        }

        public void setAreaId(int areaId) {
            this.areaId = areaId;
        }

        public List<Device> getDevices() {
            return devices;
        }

        public void setDevices(List<Device> devices) {
            this.devices = devices;
        }
    }

    public static class Device {
        @JsonProperty("hostname")
        private String hostname;

        @JsonProperty("interfaces")
        private List<Interface> interfaces;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public List<Interface> getInterfaces() {
            return interfaces;
        }

        public void setInterfaces(List<Interface> interfaces) {
            this.interfaces = interfaces;
        }
    }

    public static class Interface {
        @JsonProperty("interfaceName")
        private String interfaceName;

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }
    }
}