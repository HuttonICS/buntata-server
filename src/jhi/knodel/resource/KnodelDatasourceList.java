package jhi.knodel.resource;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnodelDatasourceList
{
	private List<KnodelDatasource> list = new ArrayList<>();

	public KnodelDatasourceList()
	{
	}

	public KnodelDatasourceList add(KnodelDatasource item)
	{
		list.add(item);
		return this;
	}

	public List<KnodelDatasource> getList()
	{
		return list;
	}

	public KnodelDatasourceList setList(List<KnodelDatasource> list)
	{
		this.list = list;
		return this;
	}

	@Override
	public String toString()
	{
		return "KnodelDatasourceList{" +
				"list=" + list +
				'}';
	}
}
