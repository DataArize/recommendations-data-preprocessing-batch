package com.dataarize.recommendations.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSet {

    private String movieId;
    private String customerId;
    private String ratings;
    private String date;
}
