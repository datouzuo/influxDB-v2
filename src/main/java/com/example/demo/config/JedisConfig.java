package com.example.demo.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfig {

	@Bean
	public JedisCluster jedisCluster(Environment env) {
		Set<HostAndPort> jedisNodes = new HashSet<>();
		String[] nodes = env.getProperty("spring.redis.cluster.nodes").split(",");
		for (String node : nodes) {
			String[] host = node.split(":");
			jedisNodes.add(new HostAndPort(host[0].trim(), Integer.parseInt(host[1].trim())));
		}

		JedisPoolConfig jedis = new JedisPoolConfig();
		// poolConfig.setMaxIdle(maxIdl
	    int maxtatal =Integer.parseInt(env.getProperty("spring.redis.cluster.poolConfig.maxActive").trim());
		jedis.setMaxTotal(maxtatal);
		jedis.setMaxIdle(Integer.parseInt(env.getProperty("spring.redis.cluster.poolConfig.maxIdle").trim()));
		jedis.setMinIdle(Integer.parseInt(env.getProperty("spring.redis.cluster.poolConfig.minIdle").trim()));
		jedis.setMaxWaitMillis(Long.parseLong(env.getProperty("spring.redis.cluster.poolConfig.maxWait").trim()));
        
		JedisCluster jedisCluster = new JedisCluster(jedisNodes,
				Integer.parseInt(env.getProperty("spring.redis.cluster.connectionTimeout").trim()),
				Integer.parseInt(env.getProperty("spring.redis.cluster.soTimeout").trim()),
				Integer.parseInt(env.getProperty("spring.redis.cluster.maxAttempts").trim()),
				env.getProperty("spring.redis.cluster.password"), jedis);

		return jedisCluster;

	}
}
