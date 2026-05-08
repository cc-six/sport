package com.sporthall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.entity.Venue;
import com.sporthall.mapper.OrderMapper;
import com.sporthall.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueMapper venueMapper;
    private final OrderMapper orderMapper;

    public Page<Venue> list(int page, int pageSize, String type) {
        QueryWrapper<Venue> wrapper = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        wrapper.eq("status", 1);
        wrapper.orderByAsc("id");
        return venueMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public List<Venue> listAll() {
        QueryWrapper<Venue> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return venueMapper.selectList(wrapper);
    }

    public Venue getById(Long id) {
        return venueMapper.selectById(id);
    }

    public void add(Venue venue) {
        validateVenue(venue);
        if (venue.getStatus() == null) {
            venue.setStatus(1);
        }
        venueMapper.insert(venue);
    }

    public void update(Venue venue) {
        validateVenue(venue);
        venueMapper.updateById(venue);
    }

    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("场地状态不合法");
        }
        Venue venue = new Venue();
        venue.setId(id);
        venue.setStatus(status);
        venueMapper.updateById(venue);
    }

    public Map<String, Boolean> getSchedule(Long venueId, LocalDate date) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) throw new RuntimeException("场地不存在");
        Map<String, Boolean> schedule = new HashMap<>();
        int start = venue.getOpenTime().getHour();
        int end = venue.getCloseTime().getHour();

        QueryWrapper<com.sporthall.entity.Order> wrapper = new QueryWrapper<>();
        wrapper.eq("venue_id", venueId)
               .eq("book_date", date)
               .in("status", 0, 1);
        List<com.sporthall.entity.Order> orders = orderMapper.selectList(wrapper);

        for (int i = start; i < end; i++) {
            String slot = String.format("%02d:00", i);
            final int hour = i;
            boolean booked = orders.stream().anyMatch(o -> {
                int oStart = o.getStartTime().getHour();
                int oEnd = o.getEndTime().getHour();
                return hour >= oStart && hour < oEnd;
            });
            schedule.put(slot, booked);
        }
        return schedule;
    }

    private void validateVenue(Venue venue) {
        if (venue.getName() == null || venue.getName().trim().isEmpty()) throw new RuntimeException("场地名称不能为空");
        if (venue.getType() == null || venue.getType().trim().isEmpty()) throw new RuntimeException("场地类型不能为空");
        if (venue.getOpenTime() == null || venue.getCloseTime() == null || !venue.getOpenTime().isBefore(venue.getCloseTime())) {
            throw new RuntimeException("开放时间不合法");
        }
        if (venue.getPricePerHour() == null || venue.getPricePerHour().signum() < 0) throw new RuntimeException("价格不能小于0");
    }
}
