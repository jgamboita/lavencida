/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contractual.utils;

/**
 *
 * @author gnoguera@CX.COL
 */
public class MathUtils {

    public static final Double ONE_HUNDRED = new Double(100);

    public static Double percentage(String base, String pct) {
        return (Double.parseDouble(base) * Double.parseDouble(pct)) / ONE_HUNDRED;
    }

}
