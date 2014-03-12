package com.proper.TestDeux.data;

/**
 * Created by Lebel on 13/02/14.
 */

public class ScanTest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private long transactionId;
    private int productId;
    private String bssId;
    private int Channel;
    private long queryTime;
    private String originatedBin;
    private String endPointLocation;

    public ScanTest() {
    }

    public ScanTest(long transactionId, int productId, String bssId, int channel, long queryTime, String originatedBin, String endPointLocation) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.bssId = bssId;
        Channel = channel;
        this.queryTime = queryTime;
        this.originatedBin = originatedBin;
        this.endPointLocation = endPointLocation;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getBssId() {
        return bssId;
    }

    public void setBssId(String bssId) {
        this.bssId = bssId;
    }

    public int getChannel() {
        return Channel;
    }

    public void setChannel(int channel) {
        Channel = channel;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    public String getOriginatedBin() {
        return originatedBin;
    }

    public void setOriginatedBin(String originatedBin) {
        this.originatedBin = originatedBin;
    }

    public String getEndPointLocation() {
        return endPointLocation;
    }

    public void setEndPointLocation(String endPointLocation) {
        this.endPointLocation = endPointLocation;
    }
}


