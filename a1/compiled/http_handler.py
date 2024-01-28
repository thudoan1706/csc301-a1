import requests

class HttpHandler:
    def __init__(self, base_url):
        self.base_url = base_url
        self.headers = {'Content-Type': 'application/json', 'Authorization': 'Bearer_Token'}

    def make_post_request(self, endpoint, data):
        try:
            url = f'{self.base_url}/{endpoint}'
            response = requests.post(url, data=data, headers=self.headers)
            self.handle_response(response, 'POST')
        except requests.RequestException as e:
            print(f"Error making POST request: {e}")

    def make_get_request(self, endpoint, params=None):
        try:
            url = f'{self.base_url}/{endpoint}'
            response = requests.get(url, params=params, headers=self.headers)

            self.handle_response(response, 'GET')
        except requests.RequestException as e:
            print(f"Error making GET request: {e}")

    def handle_response(self, response, method):
        try:
            response.raise_for_status()

            if response.status_code == 200:
                print(f"{method} request worked: {response.status_code}")
                print("Response:", response.json())
            else:
                print(f"{method} request did not work: {response.status_code}")
                print("Response:", response.text)
        except requests.RequestException as e:
            print(f"Error handling {method} response: {e}")

def make_post_request(url, data):
    try:
        headers = {'Content-Type' : 'application/json', 'Authorization' : 'Bearer_your_token'}
        response = requests.post(url, data=data, headers=headers)
        if response.status_code == 200:
            print(f"POST request did work: {response.status_code}")
            print("Response: ", response.text)
        else:
            print(f"POST request did not work: {response.status_code}")
            print("Response: ", response.text)
    except Exception as e:
        print(e)
