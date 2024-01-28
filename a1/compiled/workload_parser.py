from http_handler import HttpHandler
import sys
        
class WorkloadParser:
        
    def __init__(self, file_path: str):
        self.file_path = file_path


    def parse_workload(self):
        try:
            with open(self.file_path, 'r') as workload_file:
                for line in workload_file:
                    payload=self.parse_payload(line.strip())
                    print(payload)
                    client = HttpHandler(base_url='http://127.0.0.1:8081')
                    client.make_post_request(endpoint="/order", data=payload)
                            
        except FileNotFoundError:
            print(f"File not found: {self.file_path}")
        except Exception as e:
            print(f"An error occurred: {e}")


    def parse_payload(self, line: str):
        line_tokens = line.split()
        service = line_tokens[0].upper()
        if service == "USER":
            payload = self.parse_user_payload(line_tokens[1:])
            print(payload)
            print(payload)
        elif service == "PRODUCT":
            payload = self.parse_product_payload(line_tokens[1:])
        elif service == 'ORDER':
            payload = self.parse_order_payload(line_tokens[1:])
        elif service == 'shutdown':
            payload = {'command': service}
        elif service == 'shutdown':
            payload = {'command': service}
        else:
            raise Exception(f"Unknown service: {service}")
        return payload

    def parse_user_payload(self, line_tokens: list[str]) -> dict:
        # Define the payload and initialize user_tokens
        keys_payload = ["command", "id", "username", "email", "password"]
        user_tokens = [""] * 5

        # Mapping of token prefixes to corresponding indices in user_tokens
        token_prefix_mapping = {
            "username:un-": 2,
            "username": 2,
            "email:": 3,
            "password:": 4
        }

        # Extract values from line_tokens and clean them
        user_tokens[0] = line_tokens[0]  # Assuming "command" is at index 0
        user_tokens[1] = line_tokens[1]  # Assuming "id" is at index 1

        for i, token in enumerate(line_tokens):
            # Check for token prefixes and extract value accordingly
            for prefix, index in token_prefix_mapping.items():
                if prefix in token:
                    token = token.split(prefix)[1]
                    user_tokens[index] = token
                    
        for index, key in [(3, 'email'), (4, 'password')]:
            user_tokens[index] = '' if user_tokens[index] == '' and \
                (len(line_tokens) <= index or line_tokens[index] == '') else line_tokens[index]
        
        # Create a dictionary by pairing keys with values from user_tokens
        user_dict = {key: value for key, value in zip(keys_payload, user_tokens)}
        return user_dict
    
    
    def parse_product_payload(self, line_tokens: list[str]) -> dict:
        keys_payload = ["command", "id", "name", "description", "price", "quantity"]
        product_tokens = [""] * 6

        # Mapping of token prefixes to corresponding indices in product_tokens
        token_prefix_mapping = {
            "productname-": 2,
            "productname": 2,
            "name:": 2,
            "description:": 3,
            "price:": 4,
            "quantity:": 5
        }

        # Extract values from line_tokens and clean them
        for i, token in enumerate(line_tokens):
            try:
                # Check if the token can be converted to float
                float(token)
                product_tokens[1], product_tokens[4], product_tokens[5] = \
                (token if product_tokens[i] == "" else product_tokens[i] for i in [1, 4, 5])

            except ValueError:                    
                # Check for token prefixes and extract value accordingly
                for prefix, index in token_prefix_mapping.items():
                    if i == 0:
                        break
                    if prefix in token:
                        token = token.split(prefix)[1]
                product_tokens[i] = token.lower()

        # Create a dictionary by pairing keys with values from product_tokens
        product_dict = {key: value for key, value in zip(keys_payload, product_tokens)}
        return product_dict
    
    def parse_order_payload(self, line_tokens: list[str]):
        order_tokens = {'command': 'place_order', 'product_id':'', 'user_id': '', 'quantity': ''}

        try:
            order_tokens = {
                'command': 'place_order',
                'product_id': line_tokens[1] if len(line_tokens) > 1 else '',
                'user_id': line_tokens[2] if len(line_tokens) == 4 else '1',
                'quantity': line_tokens[3] if len(line_tokens) == 4 else (line_tokens[2] if len(line_tokens) == 3 else ''),
            }

            return order_tokens

        except IndexError:
            # Handle the case when the list index is out of range
            print("Invalid input format: Not enough tokens")
            return order_tokens
        except Exception as e:
            # Handle other exceptions if necessary
            print(f"Error: {e}")
            return order_tokens
   

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No file path provided.")
        sys.exit(1)
        
    file_path = sys.argv[1]
    
    parser = WorkloadParser(file_path=file_path)
    parser.parse_workload()
