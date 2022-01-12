package edu.jhuapl.sbmt.query.filter.model;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import com.github.davidmoten.guavamini.Lists;

public class TimeWindowFilterModel extends FilterModel
{
	@Override
	public void addFilter(FilterType filterType)
	{
		List<FilterType> allItems = Lists.newArrayList();
		allItems.addAll(getAllItems());
		allItems.add(filterType);
		setAllItems(allItems);
	}

	@Override
	public List<String> getSQLQueryString()
	{
		List queryElements = Lists.newArrayList();
		String queryString = "";
		Iterator filterIterator = getAllItems().iterator();
		while (filterIterator.hasNext())
		{
			queryString = "";
			FilterType<LocalDateTime> filter = (FilterType)filterIterator.next();
			if (!filter.isEnabled()) continue;
			Iterator<String> iterator = filter.getSQLArguments().keySet().iterator();
			queryString += filter.getQueryBaseString() + " BETWEEN ";
			queryString += filter.getSQLArguments().get("min" + filter.getQueryBaseString()) + " AND ";
			queryString += filter.getSQLArguments().get("max" + filter.getQueryBaseString()) ;
//			while (iterator.hasNext())
//			{
//				String key = iterator.next();
//				queryString += filter.getSQLArguments().get(key);
//				if (iterator.hasNext()) queryString += " AND ";
//			}
			queryElements.add(queryString);
//			if (filterIterator.hasNext()) queryString += " AND ";
		}
//		System.out.println("TimeWindowFilterModel: getSQLQueryString: query string is " + queryString);
//		return queryString;
//		System.out.println("TimeWindowFilterModel: getSQLQueryString: number of time windows " + queryElements.size());
		return queryElements;
	}
}
