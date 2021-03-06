/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dreamlu.mica.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dreamlu.mica.redis.cache.MicaRedisCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate  配置
 *
 * @author L.cm
 */
@EnableCaching
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@EnableConfigurationProperties(MicaRedisProperties.class)
public class RedisTemplateConfiguration {

	/**
	 * value 值 序列化
	 *
	 * @return RedisSerializer
	 */
	@Bean
	@ConditionalOnMissingBean(RedisSerializer.class)
	public RedisSerializer<Object> redisSerializer(MicaRedisProperties properties,
												   ObjectProvider<ObjectMapper> objectProvider) {
		MicaRedisProperties.SerializerType serializerType = properties.getSerializerType();
		if (MicaRedisProperties.SerializerType.JDK == serializerType) {
			return new JdkSerializationRedisSerializer();
		}
		// jackson findAndRegisterModules，use copy
		ObjectMapper objectMapper = objectProvider.getIfAvailable(ObjectMapper::new).copy();
		objectMapper.findAndRegisterModules();
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

	@Bean(name = "redisTemplate")
	@ConditionalOnMissingBean(RedisTemplate.class)
	public RedisTemplate<String, Object> redisTemplate(
		RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> redisSerializer) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		// key 序列化
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(keySerializer);
		redisTemplate.setHashKeySerializer(keySerializer);
		// value 序列化
		redisTemplate.setValueSerializer(redisSerializer);
		redisTemplate.setHashValueSerializer(redisSerializer);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	@Bean
	@ConditionalOnMissingBean(ValueOperations.class)
	public ValueOperations valueOperations(RedisTemplate redisTemplate) {
		return redisTemplate.opsForValue();
	}

	@Bean
	public MicaRedisCache redisClient(RedisTemplate<String, Object> redisTemplate) {
		return new MicaRedisCache(redisTemplate);
	}
}
