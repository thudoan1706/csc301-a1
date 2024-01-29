from http_handler import HttpHandler
import sys
        
class WorkloadParser:
        
    def __init__(self, file_path: str):
        self.file_path = file_path


    def parse_workload(self):
        try:
            with open(self.file_path, 'r') as workload_file:
                for line in workload_file:
                    payload=self.process_payload(line.strip())
                    client = HttpHandler(base_url='http://127.0.0.1:8081')
                    # client.make_get_request(endpoint="user/4")
                    client.make_post_request(endpoint="order", data=payload)
                            
        except FileNotFoundError:
            print(f"File not found: {self.file_path}")
        except Exception as e:
            print(f"An error occurred: {e}")

    def process_payload(self, line: str):
        line_tokens = line.split()
        service = line_tokens[0].lower()
        if service == "user":
            payload = self.parse_user_payload(line_tokens[1:])
            payload["command"]=line_tokens[1]
        elif service == "product":
            payload = self.parse_product_payload(line_tokens[1:])
            payload["command"]=line_tokens[1]
            print(payload)
        elif service == 'order':
            payload = self.parse_order_payload(line_tokens[1:])
            payload["command"]=line_tokens[1]
        elif service == 'shutdown':
            payload = {'command': 'shutdown'}
        elif service == 'restart':
            payload = {'command': 'restart'}
        else:
            raise Exception(f"Unknown service: {service}")
        return payload

    def parse_payload(self, line_tokens: list[str], keys_payload: list[str], token_prefix_mapping: dict) -> dict:
        payload_tokens = [""] * len(keys_payload)

        for i, token in enumerate(line_tokens):
            for prefix, index in token_prefix_mapping.items():
                if i == 0:
                    break
                if prefix in token:
                    token = token.split(prefix)[1]
                payload_tokens[i] = token.lower()
        # Create a dictionary by pairing keys with values from payload_tokens
        payload_dict = {key: value for key, value in zip(keys_payload, payload_tokens)}
        payload_dict["command"] = line_tokens[0]
        return payload_dict

    def parse_user_payload(self, line_tokens: list[str]) -> dict:
        keys_payload = ["command", "id", "username", "email", "password"]
        token_prefix_mapping = {
            "username:un-": 2,
            "username:": 2,
            "username": 2,
            "email:": 3,
            "password:": 4
        }
        return self.parse_payload(line_tokens, keys_payload, token_prefix_mapping)

    def parse_product_payload(self, line_tokens: list[str]) -> dict:
        keys_payload = ["command", "id", "productname", "description", "price", "quantity"]
        token_prefix_mapping = {
            "productname-": 2,
            "productname:": 2,
            "name:": 2,
            "description:": 3,
            "price:": 4,
            "quantity:": 5
        }
        return self.parse_payload(line_tokens, keys_payload, token_prefix_mapping)

    def parse_order_payload(self, line_tokens: list[str]) -> dict:
        keys_payload = ["command", "product_id", 'user_id', 'quantity']
        token_prefix_mapping = {
            "product_id:": 1,
            'user_id:': 2,
            'quantity:': 3
        }
        return self.parse_payload(line_tokens, keys_payload, token_prefix_mapping)

   

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No file path provided.")
        sys.exit(1)
        
    file_path = sys.argv[1]
    
    parser = WorkloadParser(file_path=file_path)
    parser.parse_workload()
