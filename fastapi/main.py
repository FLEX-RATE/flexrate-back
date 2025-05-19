from fastapi import FastAPI
from schemas import RateRequest, RateResponse
from predictor import predict_initial_rate

app = FastAPI()

@app.post("/predict-initial-rate", response_model=RateResponse)
def predict_rate(request: RateRequest):
    result = predict_initial_rate(request)
    return result
