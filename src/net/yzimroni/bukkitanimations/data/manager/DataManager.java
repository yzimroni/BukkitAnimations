package net.yzimroni.bukkitanimations.data.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_8_R3.Tuple;
import net.yzimroni.bukkitanimations.data.action.ActionData;

public class DataManager {

	private HashMap<Class<?>, DataHandler<?>> handlers = new HashMap<>();

	public <T> void register(Class<T> type, DataHandler<T> handler) {
		handlers.put(type, handler);
	}

	public <T> void save(ActionData action, Object object) {
		getHandlers(object).forEach(t -> t.a().save(action, object));
	}

	public <T> void save(ActionData action, Object object, Class<T> upTo) {
		getHandlers(object, upTo).forEach(t -> t.a().save(action, object));
	}

	public <T> void load(ActionData action, T object) {
		getHandlers(object).forEach(t -> t.a().load(action, object));
	}

	@SuppressWarnings("unchecked")
	public <T> List<Tuple<DataHandler<T>, Class<?>>> getHandlers(T object, Class<?> upTo) {
		List<Tuple<DataHandler<T>, Class<?>>> list = new ArrayList<>();
		Class<?> superClass = object.getClass();
		while (superClass != null && superClass != Object.class) {
			if (handlers.containsKey(superClass)) {
				list.add(new Tuple<DataHandler<T>, Class<?>>((DataHandler<T>) handlers.get(superClass), superClass));
			}
			superClass = superClass.getSuperclass();
			if (upTo != null && upTo.equals(superClass)) {
				break;
			}
		}
		Collections.reverse(list);
		return list;
	}

	public <T> List<Tuple<DataHandler<T>, Class<?>>> getHandlers(T object) {
		return getHandlers(object, null);
	}

}
