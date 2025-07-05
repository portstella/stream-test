import asyncio
import os
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
# import openai # 真实场景取消注释

# 真实场景: 从环境变量加载API Key
# openai.api_key = os.getenv("OPENAI_API_KEY")

app = FastAPI()

class PromptRequest(BaseModel):
    prompt: str

# 模拟一个异步的LLM流式生成器
async def fake_llm_stream_generator(prompt: str):
    """
    这是一个模拟的LLM流式生成器。
    在真实场景中，这里会调用 openai.ChatCompletion.create(..., stream=True)
    并遍历返回的流。
    """
    # 模拟思考时间
    await asyncio.sleep(0.5)
    
    # 模拟逐字回复
    response_text = f"这是对您的问题 '{prompt}' 的一个流式回答。我将逐字或逐词地生成内容，模拟真实AI的思考过程。\n这是第二条撒旦发生发射点"
    try:
        for char in response_text:
            yield char # 逐字返回
            await asyncio.sleep(0.05) # 模拟每个字之间的延迟
    except asyncio.CancelledError:
        print("Stream cancelled by client.")
        # 在这里可以添加任何必要的清理逻辑
        raise # 重新引发异常以确保FastAPI正确处理取消

@app.post("/stream")
async def stream_prompt(request: PromptRequest):
    """
    接收prompt，并以流的形式返回LLM的响应。
    """
    # 真实场景的调用示例:
    # response = openai.ChatCompletion.create(
    #     model="gpt-4",
    #     messages=[{"role": "user", "content": request.prompt}],
    #     stream=True
    # )
    # async def event_generator():
    #     for chunk in response:
    #         content = chunk.choices[0].delta.get("content", "")
    #         if content:
    #             yield content
    
    # 使用模拟生成器
    return StreamingResponse(fake_llm_stream_generator(request.prompt), media_type="text/plain")

# 运行命令: uvicorn llm_service:app --host 0.0.0.0 --port 8000
