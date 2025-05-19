from pydantic import BaseModel
from enum import Enum

class SexCode(int, Enum):
    male = 1
    female = 2

class RateRequest(BaseModel):
    AGE: int
    SEX_CD: SexCode
    TOTAL_SPEND: float
    min_rate: float
    max_rate: float
    credit_score: int

class RateResponse(BaseModel):
    initialRate: float
