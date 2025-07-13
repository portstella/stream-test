<script setup>
import { ref } from 'vue';

const prompt = ref('');
const streamedResponse = ref('');
const isLoading = ref(false);
let abortController = null; // 用于中止fetch请求

async function startStream() {
  if (!prompt.value) {
    alert('请输入问题！');
    return;
  }

  streamedResponse.value = '';
  isLoading.value = true;
  abortController = new AbortController(); // 为新的请求创建控制器

  try {
    // 构造符合OpenAI格式的请求体
    const requestBody = {
      model: "gpt-3.5-turbo-simulated", // 可以是任何你想要传递的模型标识
      messages: [
        { role: "user", content: prompt.value }
      ],
      stream: true
    };

    // 调用Spring Boot后端的OpenAI兼容API
    const response = await fetch('/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream' // 明确希望接收SSE流
      },
      body: JSON.stringify(requestBody),
      signal: abortController.signal // 关联AbortSignal
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      const chunk = decoder.decode(value);
      console.log("--- Raw Chunk Received ---", chunk); // 调试：打印原始数据块

      // 解析SSE事件
      const lines = chunk.split('\n').filter(line => line.trim() !== '');
      for (const line of lines) {
        console.log("Processing line:", line); // 调试：打印正在处理的行
        if (line.startsWith('data:')) {
          const data = line.substring(5);
          if (data === '[DONE]') {
            console.log("Stream finished.");
            return; // 流结束
          }
          try {
            const parsed = JSON.parse(data);
            console.log("Parsed data:", parsed); // 调试：打印解析后的JSON
            if (parsed.choices && parsed.choices[0].delta && parsed.choices[0].delta.content) {
              const content = parsed.choices[0].delta.content;
              console.log("Appending content:", content); // 调试：打印要追加的内容
              streamedResponse.value += content;
            }
          } catch (e) {
            console.error('Error parsing SSE data:', data, e);
          }
        }
      }
    }

  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('Fetch aborted by user.');
      streamedResponse.value += '\n--- 请求已取消 ---';
    } else {
      console.error('Error fetching stream:', error);
      streamedResponse.value = `错误: ${error.message}`;
    }
  } finally {
    isLoading.value = false;
    abortController = null;
  }
}

function cancelStream() {
  if (abortController) {
    abortController.abort();
  }
}
</script>

<template>
  <div class="container">
    <h1>Gemini 流式调用演示</h1>
    <p>Vue -> Spring Boot -> Python (FastAPI) -> LLM</p>

    <div class="input-area">
      <input v-model="prompt" placeholder="在这里输入你的问题..." @keyup.enter="startStream" :disabled="isLoading" />
      <button @click="startStream" :disabled="isLoading">
        {{ isLoading ? '生成中...' : '发送' }}
      </button>
      <button @click="cancelStream" v-if="isLoading" class="cancel-button">
        取消
      </button>
    </div>

    <div class="response-area">
      <h2>实时响应:</h2>
      <pre>{{ streamedResponse }}</pre>
    </div>
  </div>
</template>

<style>
.container {
  max-width: 800px;
  margin: 2rem auto;
  font-family: sans-serif;
  padding: 1rem;
}

.input-area {
  display: flex;
  gap: 10px;
  margin-bottom: 1.5rem;
}

input {
  flex-grow: 1;
  padding: 10px;
  font-size: 1rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  padding: 10px 20px;
  font-size: 1rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
}

button:disabled {
  background-color: #ccc;
}

.cancel-button {
  background-color: #f44336;
  /* 红色 */
}

.response-area {
  border: 1px solid #eee;
  padding: 1rem;
  background-color: #f9f9f9;
  min-height: 100px;
  border-radius: 4px;
}

pre {
  white-space: pre-wrap;
  /* 自动换行 */
  word-wrap: break-word;
  font-family: monospace;
}
</style>
