# FastAPI application for fingerprint matching
# To run the app, use the command:
# uvicorn app:app --reload

# adb -s 120703145D125101 reverse tcp:8000 tcp:8000
# first run in cmd shell

# uvicorn app:app --host 0.0.0.0 --port 8000

from fastapi import FastAPI, UploadFile, File, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
import cv2
import numpy as np
import json
import os

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Change in production for security
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount static files to serve photos like /static/image.png
app.mount("/static", StaticFiles(directory="database/photos"), name="static")

MATCH_THRESHOLD = 90  # minimum number of matches to consider a valid match


def read_image(file_bytes: bytes) -> np.ndarray:
    img_array = np.asarray(bytearray(file_bytes), dtype=np.uint8)
    return cv2.imdecode(img_array, cv2.IMREAD_GRAYSCALE)


def compare_fingerprints(img1: np.ndarray, img2: np.ndarray) -> int:
    orb = cv2.ORB_create()
    kp1, des1 = orb.detectAndCompute(img1, None)
    kp2, des2 = orb.detectAndCompute(img2, None)
    if des1 is None or des2 is None:
        return 0
    bf = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)
    matches = bf.match(des1, des2)
    return len(matches)


@app.post("/match-fingerprint/")
async def match_fingerprint(file: UploadFile = File(...), request: Request = None):
    try:
        contents = await file.read()
        uploaded_img = read_image(contents)
    except Exception:
        return JSONResponse(
            content={"status": "error", "message": "Failed to read uploaded image."},
            status_code=400,
        )

    try:
        with open("database/profiles.json") as f:
            profiles = json.load(f)
    except Exception as e:
        print(f"Error loading profiles.json: {e}")
        return JSONResponse(
            content={"status": "error", "message": "Failed to load profiles."},
            status_code=500,
        )

    best_score = 0
    matched_profile = None

    for profile in profiles:
        db_img_path = os.path.join("database/fingerprints", profile["fingerprint"])
        db_img = cv2.imread(db_img_path, cv2.IMREAD_GRAYSCALE)
        if db_img is None:
            print(f"Warning: fingerprint image not found for {profile['fingerprint']}")
            continue
        score = compare_fingerprints(uploaded_img, db_img)
        if score > best_score and score > MATCH_THRESHOLD:
            best_score = score
            matched_profile = profile.copy()
            host_url = str(request.base_url) if request else "http://localhost:8000/"
            matched_profile["photo"] = f"{host_url}static/{profile['photo']}"

    if matched_profile:
        return JSONResponse(
            content={"status": "success", "data": matched_profile}, status_code=200
        )
    else:
        # No match found - return default profile with image.png
        host_url = str(request.base_url) if request else "http://localhost:8000/"
        default_profile = {
            "name": "Not Found",
            "age": None,
            "photo": f"{host_url}static/image.png",
            # fingerprint key omitted or empty
        }
        return JSONResponse(
            content={"status": "fail", "data": default_profile, "message": "No match found"},
            status_code=404,
        )
