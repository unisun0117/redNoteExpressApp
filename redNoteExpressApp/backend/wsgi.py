"""WSGI entry point for PythonAnywhere deployment.
PythonAnywhere free tier only supports WSGI apps via uWSGI.
This bridges ASGI (FastAPI) to WSGI using asgiref.
"""
import os
import sys

# Add project root to Python path
path = os.path.dirname(os.path.abspath(__file__))
if path not in sys.path:
    sys.path.insert(0, path)

from asgiref.wsgi import WsgiToAsgi
from app.main import app

# Wrap FastAPI ASGI app as WSGI
application = WsgiToAsgi(app)
