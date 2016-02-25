package com.amplifino.nestor.jdbc.api;

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
		
		QueryHandler handler() {
			return handler;
		}
}
