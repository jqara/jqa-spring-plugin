package com.buschmais.jqassistant.plugin.spring.test.constraint;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.springframework.stereotype.Component;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.Result.Status;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;

/**
 * Integration tests for rules rejecting static references to injectables.
 *
 * @author Oliver Gierke
 */
public class InjectablesMustNotBeReferredToStaticallyIT extends AbstractJavaPluginIT {

	@Test
	public void reportsInjectablesInStaticFields() throws Exception {
		
		scanClasses(MyComponent.class, MyDependency.class);
		
		Result<Constraint> result = validateConstraint("spring-injection:InjectablesMustNotBeHeldInStaticVariables");
		
		store.beginTransaction();
		
		assertThat(result.getStatus(), is(Status.FAILURE));
		assertThat(result.getRows(), hasSize(1));
		
		Map<String, Object> map = result.getRows().get(0);
		TypeDescriptor descriptor = (TypeDescriptor) map.get("Type");
		
		assertThat(descriptor, TypeDescriptorMatcher.typeDescriptor(MyComponent.class));
		assertThat(map.get("Field"), is((Object) "dependency"));
		
		store.rollbackTransaction();
	}

	@Test
	public void reportsStaticReferenceToInjectable() throws Exception {
		
		scanClasses(MyComponent.class, MyDependency.class);
		
		Result<Constraint> result = validateConstraint("spring-injection:InjectablesMustNotBeAccessedStatically");
		
		store.beginTransaction();
		
		assertThat(result.getStatus(), is(Status.FAILURE));
		assertThat(result.getRows(), hasSize(1));
		
		Map<String, Object> map = result.getRows().get(0);
		MethodDescriptor descriptor = (MethodDescriptor) map.get("Method");
		
		assertThat(descriptor, MethodDescriptorMatcher.methodDescriptor(MyDependency.class, "someMethod"));
		assertThat(map.get("Field"), is((Object) "dependency"));
		
		store.rollbackTransaction();
	}
	
	@Component
	static class MyComponent {
		static MyDependency dependency;
	}
	
	@Component
	static class MyDependency {
		
		public static MyDependency someMethod() {
			return MyComponent.dependency;
		}
	}
}
