/*
 * Copyright (c) 2020
 * Contributed by Mario Teklic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htw.berlin.steganography.apis.models;

import java.util.Date;

/**
 * @author Mario Teklic
 */


public class MyDate implements Comparable<MyDate>{

    /**
     * Date
     */
    private Date date;

    /**
     * Default constructor. Should not be used because some networks
     * deliver timestamps with seconds instead of milliseconds. This does not work with
     * the Java Date object
     */
    public MyDate(){}

    /**
     * Multiplies a dates getTime() value by 1000 which will results in a ms value instead of a seconds value
     * @param date which has incorrect value. Seconds instead ms.
     */
    public MyDate(Date date) {
        if(date.getTime() > 1000 && String.valueOf(date.getTime()).endsWith("000")){
            this.date = new Date(date.getTime());
        }else{
            this.date = new Date(date.getTime() * 1000);
        }
    }

    public long getTime(){
        return this.date.getTime();
    }

    public void setDate(Date date){
        this.date = date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(MyDate opposite) {
        return Long.valueOf(this.getTime()).compareTo(opposite.getTime());
    }
}
