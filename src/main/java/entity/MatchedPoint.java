package entity;

import java.util.Date;

public class MatchedPoint extends Point
{
    private String road_id;
    private String car_id;
    private Date timestamp;
    private double deviation;

    public MatchedPoint(String road_id,String car_id,Date timestamp,double longitude,double latitude,double deviation)
    {
        super(longitude,latitude);
        this.road_id = road_id;
        this.car_id = car_id;
        this.timestamp = timestamp;
        this.deviation = deviation;
    }

    public String getRoad_id() {
        return road_id;
    }

    public String getCar_id() {
        return car_id;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public double getDeviation()
    {
        return deviation;
    }

    public String toString()
    {
        return
                this.road_id+","+this.car_id+","+this.longitude+","+this.latitude+","+this.deviation;
                //this.road_id+","+this.car_id+","+this.longitude+","+this.latitude+","+this.timestamp.toString()+","+this.deviation;
    }
}
