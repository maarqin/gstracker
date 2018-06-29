package com.gstracker.cordova.plugin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompositePKPosition {

    @Expose
    @SerializedName("tpctmtID")
    private Integer tpctmtID;

    @Expose
    @SerializedName("tpcDataHoraExibicao")
    private String tpcDataHoraExibicao;

    public CompositePKPosition() {
    }

    public CompositePKPosition(Integer tpctmtID, String tpcDataHoraExibicao) {
        this.tpctmtID = tpctmtID;
        this.tpcDataHoraExibicao = tpcDataHoraExibicao;
    }

    public Integer getTpctmtID() {
        return tpctmtID;
    }

    public void setTpctmtID(Integer tpctmtID) {
        this.tpctmtID = tpctmtID;
    }

    public String getTpcDataHoraExibicao() {
        return tpcDataHoraExibicao;
    }

    public void setTpcDataHoraExibicao(String tpcDataHoraExibicao) {
        this.tpcDataHoraExibicao = tpcDataHoraExibicao;
    }

    @Override
    public String toString() {
        return "CompositePKPosition{" +
                "tpctmtID=" + tpctmtID +
                ", tpcDataHoraExibicao='" + tpcDataHoraExibicao + '\'' +
                '}';
    }
}
