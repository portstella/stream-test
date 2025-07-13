import asyncio
import json
import time
import uuid
from typing import List, Optional

from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

app = FastAPI()

# --- Pydantic Models for OpenAI compatibility ---

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatCompletionRequest(BaseModel):
    model: str
    messages: List[ChatMessage]
    stream: Optional[bool] = False
    # Add other OpenAI parameters as needed, e.g., temperature, max_tokens

async def openai_format_stream_generator(messages: List[ChatMessage]):
    """
    Simulates a streaming response in the format of OpenAI's Chat Completions API.
    Yields data in Server-Sent Events (SSE) format.
    """
    chat_id = f"chatcmpl-{uuid.uuid4()}"
    created_timestamp = int(time.time())
    
    # Extract the last user prompt to include in the response
    user_prompt = "your prompt"
    if messages and messages[-1].role == "user":
        user_prompt = messages[-1].content

    response_text = f"This is a simulated \n\nstreaming response to your prompt: '{user_prompt}'. I am generating token by token to mimic the real OpenAI API behavior."
    
    # 1. First chunk: Send the role
    first_chunk_payload = {
        "id": chat_id,
        "object": "chat.completion.chunk",
        "created": created_timestamp,
        "model": "gpt-3.5-turbo-simulated",
        "choices": [
            {
                "index": 0,
                "delta": {"role": "assistant", "content": ""},
                "finish_reason": None
            }
        ]
    }
    yield f"data: {json.dumps(first_chunk_payload)}\n\n"
    await asyncio.sleep(0.05) # Small delay

    # 2. Subsequent chunks: Send content word by word
    try:
        for word in response_text.split(" "):
            chunk_payload = {
                "id": chat_id,
                "object": "chat.completion.chunk",
                "created": created_timestamp,
                "model": "gpt-3.5-turbo-simulated",
                "choices": [
                    {
                        "index": 0,
                        "delta": {"content": f"{word} "},
                        "finish_reason": None
                    }
                ]
            }
            yield f"data: {json.dumps(chunk_payload)}\n\n"
            await asyncio.sleep(0.1) # Simulate token generation delay

        # 3. Final chunk: Signal the end of the stream
        final_chunk_payload = {
            "id": chat_id,
            "object": "chat.completion.chunk",
            "created": created_timestamp,
            "model": "gpt-3.5-turbo-simulated",
            "choices": [
                {
                    "index": 0,
                    "delta": {},
                    "finish_reason": "stop"
                }
            ]
        }
        yield f"data: {json.dumps(final_chunk_payload)}\n\n"

        # 4. Termination signal
        yield "data: [DONE]\n\n"
        
    except asyncio.CancelledError:
        print("Stream cancelled by client.")
        raise


@app.post("/v1/chat/completions")
async def chat_completions(request: ChatCompletionRequest):
    """
    An endpoint that mimics OpenAI's chat completions streaming API.
    """
    return StreamingResponse(
        openai_format_stream_generator(request.messages),
        media_type="text/event-stream"
    )

# To run this service:
# uvicorn llm_service:app --host 0.0.0.0 --port 8001