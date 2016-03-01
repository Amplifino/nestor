package com.amplifino.nestor.jdbc.api;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractQuery implements Query {
		
		private final QueryHandler handler = new QueryHandler();
		
		@Override
		public Query text(String sql) {
			handler.text(sql);
			return this;
		}

		@Override
		public Query parameters(Object parameter, Object... parameters) {
			handler.parameters(parameter, parameters);
			return this;
		}
		
		@Override
		public Query limit(int limit) {
			handler.limit(limit);
			return this;
		}
		
		@Override
		public Query fetchSize(int fetchSize) {
			handler.fetchSize(fetchSize);
			return this;
		}
		
		@Override
		final public <T> List<T> select(TupleParser<T> parser) {
			List<T> result = new ArrayList<>();
			select(parser, result::add);
			return result;
		}
		
		QueryHandler handler() {
			return handler;
		}
}
