package it.passolimirko.memorandum.room.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "memo", indices = {
        @Index(value = {"status", "date"})
})
public class Memo implements Parcelable {
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_COMPLETED = 1;

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "latitude", defaultValue = "200")
    public double latitude;

    @ColumnInfo(name = "longitude", defaultValue = "200")
    public double longitude;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "status", defaultValue = "0")
    public int status;

    public Memo() {}

    public Memo(String title, String content, double latitude, double longitude, Date date, int status) {
        this.title = title;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.status = status;
    }

    protected Memo(Parcel in) {
        id = in.readInt();
        title = in.readString();
        content = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        date = new Date(in.readLong() * 1000);
        status = in.readInt();
    }

    public static final Creator<Memo> CREATOR = new Creator<Memo>() {
        @Override
        public Memo createFromParcel(Parcel in) {
            return new Memo(in);
        }

        @Override
        public Memo[] newArray(int size) {
            return new Memo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(date.getTime() / 1000);
        dest.writeInt(status);
    }
}
