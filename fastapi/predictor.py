import joblib
import pandas as pd
import numpy as np
from schemas import RateRequest, RateResponse

# 모델과 내부 금리 정규화 기준
model_bundle = joblib.load("model/normalized_rate_model_with_bounds.pkl")
model = model_bundle["model"]
global_min_rate = model_bundle["min_rate"]
global_max_rate = model_bundle["max_rate"]

# 금리 조정 함수
def adjust_rate_by_credit(predicted_rate, credit_score, min_rate, max_rate):
    if credit_score == 600:
        return predicted_rate

    adjustment_strength = 0.005 * abs(credit_score - 600) / 400  # 최대 0.005
    if credit_score < 600:
        adjusted = predicted_rate + adjustment_strength * (max_rate - min_rate)
    else:
        adjusted = predicted_rate - adjustment_strength * (max_rate - min_rate)

    return max(min(adjusted, max_rate), min_rate)

# 예측 함수
def predict_initial_rate(request: RateRequest) -> RateResponse:
    input_df = pd.DataFrame([{
        "AGE": request.AGE,
        "SEX_CD": request.SEX_CD,
        "TOTAL_SPEND": request.TOTAL_SPEND
    }])

    normalized = model.predict(input_df)[0]
    biased = normalized ** 0.3

    raw_rate = request.min_rate + biased * (request.max_rate - request.min_rate)
    final_rate = adjust_rate_by_credit(raw_rate, request.credit_score, request.min_rate, request.max_rate)

    return RateResponse(initialRate=round(final_rate, 2))
