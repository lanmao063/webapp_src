package com.neu.webapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.webapp.entity.Parcel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ParcelMapper extends BaseMapper<Parcel> {

    @Select("SELECT MAX(sequence_no) FROM parcel WHERE day_of_week = #{dayOfWeek} AND cabinet_type = #{cabinetType}")
    Integer getMaxSequenceNo(@Param("dayOfWeek") int dayOfWeek, @Param("cabinetType") String cabinetType);
}
