package com.projeto.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração para habilitar execução assíncrona e agendamento de tarefas.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
	
}